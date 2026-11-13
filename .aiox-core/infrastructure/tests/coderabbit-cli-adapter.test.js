'use strict';

const assert = require('node:assert/strict');
const childProcess = require('node:child_process');
const fs = require('node:fs/promises');
const Module = require('node:module');
const os = require('node:os');
const path = require('node:path');
const test = require('node:test');
const { CodeRabbitCliAdapter, buildInvocation } = require('../../core/quality-gates/coderabbit-cli-adapter');

const authSuccess = JSON.stringify({ status: 'authenticated', authenticated: true });

function completeEvent(overrides = {}) {
  return {
    type: 'complete',
    status: 'review_completed',
    outcome: 'completed',
    reused: false,
    unreviewedFileCount: 0,
    gate: {
      threshold: 'critical',
      status: 'passed',
      blockingFindings: 0,
    },
    ...overrides,
  };
}

function reviewOutput(projectRoot, complete = completeEvent(), otherEvents = []) {
  return [
    { type: 'review_context', workingDirectory: projectRoot },
    ...otherEvents,
    complete,
  ]
    .map((event) => JSON.stringify(event))
    .join('\n');
}

async function createHarness(t, options = {}) {
  const root = await fs.mkdtemp(path.join(os.tmpdir(), 'coderabbit adapter test-'));
  t.after(() => fs.rm(root, { recursive: true, force: true }));

  const projectRoot = path.join(root, 'synthetic repo with spaces');
  const fakeBinDir = path.join(root, 'fake tools');
  const cliPath = path.join(fakeBinDir, 'coderabbit');
  const logPath = path.join(root, 'argv.jsonl');
  await fs.mkdir(projectRoot, { recursive: true });
  await fs.mkdir(fakeBinDir, { recursive: true });
  await fs.writeFile(path.join(projectRoot, 'source.txt'), 'synthetic source\n');
  childProcess.execFileSync('git', ['init', '-b', 'main'], { cwd: projectRoot });
  childProcess.execFileSync('git', ['config', 'user.email', 'test@example.invalid'], {
    cwd: projectRoot,
  });
  childProcess.execFileSync('git', ['config', 'user.name', 'Synthetic Test'], {
    cwd: projectRoot,
  });
  childProcess.execFileSync('git', ['add', 'source.txt'], { cwd: projectRoot });
  childProcess.execFileSync('git', ['commit', '-m', 'synthetic base'], { cwd: projectRoot });

  const script = `#!/usr/bin/env node
const fs = require('node:fs');
const childProcess = require('node:child_process');
const args = process.argv.slice(2);
fs.appendFileSync(process.env.FAKE_LOG, JSON.stringify(args) + '\\n');
if (args[0] === '--version') {
  process.stdout.write(process.env.FAKE_VERSION || 'coderabbit 0.9.5\\n');
} else if (args[0] === 'auth') {
  process.stdout.write(process.env.FAKE_AUTH || '{"status":"authenticated","authenticated":true}\\n');
} else if (args[0] === 'review') {
  const delay = Number(process.env.FAKE_DELAY_MS || 0);
  if (delay > 0) Atomics.wait(new Int32Array(new SharedArrayBuffer(4)), 0, 0, delay);
  if (process.env.FAKE_CHANGE_HEAD === '1') {
    childProcess.spawnSync('git', ['-C', process.env.FAKE_GIT_ROOT, 'commit', '--allow-empty', '-m', 'synthetic concurrent change'], { stdio: 'ignore' });
  }
  process.stdout.write(process.env.FAKE_REVIEW || '');
  process.exitCode = Number(process.env.FAKE_REVIEW_EXIT || 0);
}
`;
  await fs.writeFile(cliPath, script, { mode: 0o755 });

  const env = {
    ...process.env,
    FAKE_LOG: logPath,
    FAKE_GIT_ROOT: projectRoot,
    FAKE_AUTH: options.auth || authSuccess,
    FAKE_REVIEW: options.review || reviewOutput(projectRoot),
    FAKE_REVIEW_EXIT: String(options.exitCode ?? 0),
    FAKE_VERSION: options.version || 'coderabbit 0.9.5\n',
    FAKE_DELAY_MS: String(options.delayMs || 0),
    FAKE_CHANGE_HEAD: options.changeHead ? '1' : '0',
  };

  return { root, projectRoot, cliPath, logPath, env };
}

function createAdapter(harness, options = {}) {
  return new CodeRabbitCliAdapter(
    {
      cli_path: harness.cliPath,
      installation_mode: 'native',
      timeout: options.timeout || 5000,
    },
    { projectRoot: harness.projectRoot, env: harness.env },
  );
}

async function readInvocations(logPath) {
  const content = await fs.readFile(logPath, 'utf8');
  return content
    .trim()
    .split('\n')
    .filter(Boolean)
    .map((line) => JSON.parse(line));
}

async function withFakeEnvironment(env, callback) {
  const keys = [
    'FAKE_LOG',
    'FAKE_GIT_ROOT',
    'FAKE_AUTH',
    'FAKE_REVIEW',
    'FAKE_REVIEW_EXIT',
    'FAKE_VERSION',
    'FAKE_DELAY_MS',
    'FAKE_CHANGE_HEAD',
  ];
  const previous = Object.fromEntries(keys.map((key) => [key, process.env[key]]));
  for (const key of keys) process.env[key] = env[key];
  try {
    return await callback();
  } finally {
    for (const key of keys) {
      if (previous[key] === undefined) delete process.env[key];
      else process.env[key] = previous[key];
    }
  }
}

function loadWorkflowExecutorWithYamlStub() {
  const originalLoad = Module._load;
  Module._load = function (request, parent, isMain) {
    if (request === 'js-yaml') return { load: () => ({}), dump: () => '' };
    return originalLoad.call(this, request, parent, isMain);
  };
  try {
    return require('../../core/orchestration/workflow-executor').WorkflowExecutor;
  } finally {
    Module._load = originalLoad;
  }
}

test('runs a supported uncommitted review through the structured CLI contract', async (t) => {
  const harness = await createHarness(t);
  const result = await createAdapter(harness).run();

  assert.equal(result.success, true);
  assert.deepEqual(result.issues, []);
  const invocations = await readInvocations(harness.logPath);
  assert.deepEqual(invocations[0], ['--version']);
  assert.deepEqual(invocations[1], ['auth', 'status', '--agent']);
  assert.deepEqual(invocations[2], [
    'review',
    '--agent',
    '--uncommitted',
    '--gate',
    'critical',
  ]);
});

test('returns complete critical findings as findings rather than operational failures', async (t) => {
  const harness = await createHarness(t);
  harness.env.FAKE_REVIEW = reviewOutput(
    harness.projectRoot,
    completeEvent({
      gate: { threshold: 'critical', status: 'blocked', blockingFindings: 1 },
    }),
    [
      {
        type: 'finding',
        severity: 'critical',
        fileName: 'src/example.js',
        title: 'Synthetic critical issue',
        comment: 'Synthetic finding for a local test.',
        startLine: 4,
        endLine: 5,
      },
    ],
  );
  harness.env.FAKE_REVIEW_EXIT = '2';

  const result = await createAdapter(harness).run();
  assert.equal(result.success, true);
  assert.equal(result.issues[0].severity, 'CRITICAL');
  assert.equal(result.issues[0].file, 'src/example.js');
  assert.equal(result.issues[0].line, 4);
});

test('blocks before review when authentication is not confirmed', async (t) => {
  const harness = await createHarness(t, {
    auth: JSON.stringify({ status: 'not_authenticated', authenticated: false }),
  });
  const result = await createAdapter(harness).run();

  assert.equal(result.success, false);
  assert.equal(result.blocked, true);
  assert.equal(result.error, 'authentication_unavailable');
  assert.deepEqual(await readInvocations(harness.logPath), [
    ['--version'],
    ['auth', 'status', '--agent'],
  ]);
});

test('blocks malformed authentication output and unsupported CLI versions', async (t) => {
  const malformed = await createHarness(t, { auth: 'not json' });
  const malformedResult = await createAdapter(malformed).run();
  assert.equal(malformedResult.error, 'authentication_unavailable');

  const unsupported = await createHarness(t, { version: 'coderabbit 1.0.0\n' });
  const unsupportedResult = await createAdapter(unsupported).run();
  assert.equal(unsupportedResult.error, 'cli_version_unsupported');
  assert.deepEqual(await readInvocations(unsupported.logPath), [['--version']]);

  const belowMinimum = await createHarness(t, { version: 'coderabbit 0.9.0\n' });
  assert.equal((await createAdapter(belowMinimum).run()).error, 'cli_version_unsupported');
});

test('blocks malformed, failed, and non-terminal NDJSON reviews', async (t) => {
  const malformed = await createHarness(t, { review: '{"type":"review_context"}\n{bad json' });
  assert.equal((await createAdapter(malformed).run()).error, 'review_output_invalid');

  const operationalFailure = await createHarness(t, { exitCode: 1 });
  assert.equal(
    (await createAdapter(operationalFailure).run()).error,
    'review_result_inconsistent',
  );

  const withError = await createHarness(t);
  withError.env.FAKE_REVIEW = reviewOutput(
    withError.projectRoot,
    completeEvent(),
    [{ type: 'error', errorType: 'synthetic' }],
  );
  assert.equal((await createAdapter(withError).run()).error, 'review_reported_error');

  const missingComplete = await createHarness(t);
  missingComplete.env.FAKE_REVIEW = JSON.stringify({
    type: 'review_context',
    workingDirectory: missingComplete.projectRoot,
  });
  assert.equal((await createAdapter(missingComplete).run()).error, 'review_completion_invalid');

  const duplicateComplete = await createHarness(t);
  duplicateComplete.env.FAKE_REVIEW = [
    JSON.stringify({ type: 'review_context', workingDirectory: duplicateComplete.projectRoot }),
    JSON.stringify(completeEvent()),
    JSON.stringify(completeEvent()),
  ].join('\n');
  assert.equal((await createAdapter(duplicateComplete).run()).error, 'review_completion_invalid');

  const nonTerminal = await createHarness(t);
  nonTerminal.env.FAKE_REVIEW = [
    JSON.stringify({ type: 'review_context', workingDirectory: nonTerminal.projectRoot }),
    JSON.stringify(completeEvent()),
    JSON.stringify({ type: 'status', status: 'late' }),
  ].join('\n');
  assert.equal((await createAdapter(nonTerminal).run()).error, 'review_completion_not_terminal');
});

test('blocks reused or incomplete reviews and unverified scopes', async (t) => {
  const reused = await createHarness(t, {
    review: '',
  });
  reused.env.FAKE_REVIEW = reviewOutput(
    reused.projectRoot,
    completeEvent({ reused: true }),
  );
  assert.equal((await createAdapter(reused).run()).error, 'review_incomplete');

  const incomplete = await createHarness(t);
  incomplete.env.FAKE_REVIEW = reviewOutput(
    incomplete.projectRoot,
    completeEvent({ outcome: 'failed', unreviewedFileCount: 2 }),
  );
  assert.equal((await createAdapter(incomplete).run()).error, 'review_incomplete');

  const unknownOutcome = await createHarness(t);
  unknownOutcome.env.FAKE_REVIEW = reviewOutput(
    unknownOutcome.projectRoot,
    completeEvent({ outcome: 'unknown' }),
  );
  assert.equal((await createAdapter(unknownOutcome).run()).error, 'review_incomplete');

  const wrongScope = await createHarness(t);
  wrongScope.env.FAKE_REVIEW = reviewOutput('/tmp/other-worktree');
  assert.equal((await createAdapter(wrongScope).run()).error, 'review_scope_unverified');
});

test('blocks a review that exceeds its timeout', async (t) => {
  const harness = await createHarness(t, { delayMs: 1000 });
  const result = await createAdapter(harness, { timeout: 50 }).run();

  assert.equal(result.success, false);
  assert.equal(result.error, 'cli_timeout');
});

test('blocks when the configured CLI executable is missing', async (t) => {
  const harness = await createHarness(t);
  const adapter = new CodeRabbitCliAdapter(
    {
      cli_path: path.join(harness.root, 'missing coderabbit'),
      installation_mode: 'native',
    },
    { projectRoot: harness.projectRoot },
  );
  const result = await adapter.run();

  assert.equal(result.success, false);
  assert.equal(result.blocked, true);
  assert.equal(result.error, 'cli_not_found');
});

test('requires a valid base SHA and records a stable committed range', async (t) => {
  const harness = await createHarness(t);
  const baseSha = childProcess
    .execFileSync('git', ['rev-parse', 'HEAD'], { cwd: harness.projectRoot, encoding: 'utf8' })
    .trim();
  await fs.appendFile(path.join(harness.projectRoot, 'source.txt'), 'head commit\n');
  childProcess.execFileSync('git', ['add', 'source.txt'], { cwd: harness.projectRoot });
  childProcess.execFileSync('git', ['commit', '-m', 'synthetic head'], {
    cwd: harness.projectRoot,
  });
  harness.env.FAKE_REVIEW = reviewOutput(harness.projectRoot);

  const result = await createAdapter(harness).run({ scope: 'committed', baseSha });
  assert.equal(result.success, true);
  assert.equal(result.range.baseSha, baseSha);
  assert.equal(result.range.headSha, result.range.headShaAfter);
  const reviewArgs = (await readInvocations(harness.logPath))[2];
  assert.deepEqual(reviewArgs, [
    'review',
    '--agent',
    '--committed',
    '--base-commit',
    baseSha,
    '--gate',
    'critical',
  ]);

  const invalidSha = await createAdapter(harness).run({
    scope: 'committed',
    baseSha: 'deadbeef',
  });
  assert.equal(invalidSha.error, 'commit_sha_invalid');
});

test('blocks if HEAD changes while a committed review is running', async (t) => {
  const harness = await createHarness(t, { changeHead: true });
  const baseSha = childProcess
    .execFileSync('git', ['rev-parse', 'HEAD'], { cwd: harness.projectRoot, encoding: 'utf8' })
    .trim();
  await fs.appendFile(path.join(harness.projectRoot, 'source.txt'), 'head commit\n');
  childProcess.execFileSync('git', ['add', 'source.txt'], { cwd: harness.projectRoot });
  childProcess.execFileSync('git', ['commit', '-m', 'synthetic head'], {
    cwd: harness.projectRoot,
  });
  harness.env.FAKE_REVIEW = reviewOutput(harness.projectRoot);

  const result = await createAdapter(harness).run({ scope: 'committed', baseSha });
  assert.equal(result.success, false);
  assert.equal(result.error, 'checkout_changed_during_review');
});

test('builds native argv and safely quoted WSL commands without shell execution', () => {
  const native = buildInvocation({
    cliPath: '/tmp/fake cli/coderabbit',
    mode: 'native',
    projectRoot: '/tmp/project root',
    args: ['review', '--agent', '--uncommitted'],
  });
  assert.equal(native.command, '/tmp/fake cli/coderabbit');
  assert.deepEqual(native.args, ['review', '--agent', '--uncommitted']);

  const wsl = buildInvocation({
    cliPath: '~/.local/bin/coderabbit',
    mode: 'wsl',
    projectRoot: "C:\\Users\\Owner\\Qas repo\\O'Brien",
    args: ['review', '--agent', '--uncommitted'],
    hostPlatform: 'win32',
  });
  assert.equal(wsl.command, 'wsl');
  assert.deepEqual(wsl.args.slice(0, 2), ['bash', '-lc']);
  assert.match(wsl.args[2], /\/mnt\/c\/Users\/Owner\/Qas repo\/O'\\''Brien/);
  assert.match(wsl.args[2], /"\$HOME"\/'\.local\/bin\/coderabbit'/);
});

test('Layer 2 consumes the shared adapter and blocks operational failures', async (t) => {
  const { Layer2PRAutomation } = require('../../core/quality-gates/layer2-pr-automation');
  const harness = await createHarness(t, {
    review: '',
  });
  harness.env.FAKE_REVIEW = reviewOutput(harness.projectRoot);
  const layer = new Layer2PRAutomation({
    coderabbit: {
      cli_path: harness.cliPath,
      installation_mode: 'native',
      projectRoot: harness.projectRoot,
      timeout: 5000,
    },
    quinn: { enabled: false },
  });

  const result = await withFakeEnvironment(harness.env, () => layer.runCodeRabbit());
  assert.equal(result.pass, true, JSON.stringify(result));

  harness.env.FAKE_REVIEW = reviewOutput(
    harness.projectRoot,
    completeEvent({
      gate: { threshold: 'critical', status: 'blocked', blockingFindings: 1 },
    }),
    [
      {
        type: 'finding',
        severity: 'critical',
        fileName: 'src/example.js',
        title: 'Synthetic critical issue',
      },
    ],
  );
  harness.env.FAKE_REVIEW_EXIT = '2';
  const finding = await withFakeEnvironment(harness.env, () => layer.runCodeRabbit());
  assert.equal(finding.pass, false);
  assert.equal(finding.issues.critical, 1);

  harness.env.FAKE_AUTH = JSON.stringify({
    status: 'not_authenticated',
    authenticated: false,
  });
  const blocked = await withFakeEnvironment(harness.env, () => layer.runCodeRabbit());
  assert.equal(blocked.pass, false);
  assert.equal(blocked.blocked, true);
  assert.equal(blocked.skipped, undefined);
});

test('WorkflowExecutor uses the adapter and fails closed on authentication errors', async (t) => {
  const WorkflowExecutor = loadWorkflowExecutorWithYamlStub();
  const harness = await createHarness(t);
  const executor = new WorkflowExecutor(harness.projectRoot, { saveState: false });
  const config = {
    cli_path: harness.cliPath,
    installation_mode: 'native',
    self_healing: { timeout_minutes: 1, max_iterations: 2 },
  };

  const successful = await withFakeEnvironment(harness.env, () =>
    executor.runCodeRabbitAnalysis(config),
  );
  assert.equal(successful.success, true);
  assert.deepEqual(successful.issues, []);

  harness.env.FAKE_AUTH = JSON.stringify({
    status: 'not_authenticated',
    authenticated: false,
  });
  executor.config = {
    coderabbit_integration: { enabled: true, ...config },
  };
  const failedPhase = await withFakeEnvironment(harness.env, () =>
    executor.executeSelfHealingPhase({ config: { max_iterations: 1 } }, 'dev'),
  );
  assert.equal(failedPhase.status, 'failed');
  assert.equal(failedPhase.blocked, true);
  assert.equal(failedPhase.error, 'authentication_unavailable');
});
