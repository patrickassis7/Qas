'use strict';

const childProcess = require('child_process');
const os = require('os');
const path = require('path');

const MAX_OUTPUT_BYTES = 10 * 1024 * 1024;
const SUPPORTED_MAJOR = 0;
const SUPPORTED_MINOR = 9;
const MIN_SUPPORTED_PATCH = 1;

class CodeRabbitAdapterError extends Error {
  constructor(code) {
    super(code);
    this.name = 'CodeRabbitAdapterError';
    this.code = code;
  }
}

class CodeRabbitCliAdapter {
  constructor(config = {}, options = {}) {
    this.config = config;
    this.projectRoot = path.resolve(options.projectRoot || config.projectRoot || process.cwd());
    this.cliPath = config.cli_path || '~/.local/bin/coderabbit';
    this.mode =
      config.installation_mode || (process.platform === 'win32' ? 'wsl' : 'native');
    this.timeoutMs = config.timeout || 900000;
    this.spawn = options.spawn || childProcess.spawn;
    this.env = options.env || process.env;
    this.hostPlatform = options.hostPlatform || process.platform;
  }

  async run({ scope = 'uncommitted', baseSha, headSha } = {}) {
    const startedAt = Date.now();

    try {
      if (!['uncommitted', 'committed'].includes(scope)) {
        throw new CodeRabbitAdapterError('review_scope_invalid');
      }

      const versionResult = await this.execute(['--version'], 30000);
      if (versionResult.exitCode !== 0) {
        throw new CodeRabbitAdapterError('cli_version_check_failed');
      }
      const version = parseSupportedVersion(versionResult.stdout);
      if (!version) {
        throw new CodeRabbitAdapterError('cli_version_unsupported');
      }

      const authResult = await this.execute(['auth', 'status', '--agent'], 30000);
      if (authResult.exitCode !== 0 || !isAuthenticated(authResult.stdout)) {
        throw new CodeRabbitAdapterError('authentication_unavailable');
      }

      let range;
      if (scope === 'committed') {
        range = this.resolveCommittedRange(baseSha, headSha);
      }

      const reviewArgs = ['review', '--agent'];
      if (scope === 'committed') {
        reviewArgs.push('--committed', '--base-commit', range.baseSha);
      } else {
        reviewArgs.push('--uncommitted');
      }
      if (version.patch >= 1) {
        reviewArgs.push('--gate', 'critical');
      }

      const reviewResult = await this.execute(reviewArgs, this.timeoutMs);
      const review = parseReviewOutput(reviewResult.stdout, {
        expectedDirectory: this.getExpectedReviewDirectory(),
        exitCode: reviewResult.exitCode,
      });

      if (scope === 'committed') {
        const headAfter = this.readHead();
        if (headAfter !== range.headSha) {
          throw new CodeRabbitAdapterError('checkout_changed_during_review');
        }
        range.headShaAfter = headAfter;
      }

      return {
        success: true,
        issues: review.issues,
        scope,
        version: version.raw,
        gate: review.gate,
        range,
        duration: Date.now() - startedAt,
      };
    } catch (error) {
      return {
        success: false,
        blocked: true,
        error: error instanceof CodeRabbitAdapterError ? error.code : 'adapter_execution_failed',
        issues: [],
        duration: Date.now() - startedAt,
      };
    }
  }

  async execute(args, timeoutMs) {
    const invocation = buildInvocation({
      cliPath: this.cliPath,
      mode: this.mode,
      projectRoot: this.projectRoot,
      args,
      hostPlatform: this.hostPlatform,
    });

    return runProcess(invocation, timeoutMs, this.spawn, this.env);
  }

  resolveCommittedRange(baseSha, expectedHeadSha) {
    if (!isFullSha(baseSha) || (expectedHeadSha && !isFullSha(expectedHeadSha))) {
      throw new CodeRabbitAdapterError('commit_sha_invalid');
    }

    const headSha = this.readHead();
    if (expectedHeadSha && headSha !== expectedHeadSha.toLowerCase()) {
      throw new CodeRabbitAdapterError('checkout_head_mismatch');
    }

    const baseCheck = childProcess.spawnSync('git', ['cat-file', '-e', `${baseSha}^{commit}`], {
      cwd: this.projectRoot,
      encoding: 'utf8',
      stdio: ['ignore', 'ignore', 'ignore'],
    });
    if (baseCheck.error || baseCheck.status !== 0) {
      throw new CodeRabbitAdapterError('base_commit_unavailable');
    }

    return { baseSha: baseSha.toLowerCase(), headSha };
  }

  readHead() {
    const result = childProcess.spawnSync('git', ['rev-parse', 'HEAD'], {
      cwd: this.projectRoot,
      encoding: 'utf8',
      stdio: ['ignore', 'pipe', 'ignore'],
    });
    const headSha = result.stdout?.trim();
    if (result.error || result.status !== 0 || !isFullSha(headSha)) {
      throw new CodeRabbitAdapterError('checkout_head_unavailable');
    }
    return headSha.toLowerCase();
  }

  getExpectedReviewDirectory() {
    return this.mode === 'wsl' ? windowsPathToWsl(this.projectRoot) : this.projectRoot;
  }
}

function buildInvocation({ cliPath, mode, projectRoot, args, hostPlatform = process.platform }) {
  if (typeof cliPath !== 'string' || !cliPath || !Array.isArray(args)) {
    throw new CodeRabbitAdapterError('cli_configuration_invalid');
  }
  if (!['native', 'wsl'].includes(mode)) {
    throw new CodeRabbitAdapterError('installation_mode_invalid');
  }

  if (mode === 'native') {
    const resolvedCliPath =
      cliPath === '~'
        ? os.homedir()
        : cliPath.startsWith('~/')
          ? path.resolve(os.homedir(), cliPath.slice(2))
          : cliPath;
    return {
      command: resolvedCliPath,
      args,
      cwd: projectRoot,
    };
  }

  const wslRoot = windowsPathToWsl(projectRoot, hostPlatform);
  const cliCommand = renderWslCliPath(cliPath);
  const script = [
    `cd ${shellQuote(wslRoot)}`,
    `exec ${cliCommand} ${args.map(shellQuote).join(' ')}`,
  ].join(' && ');

  return {
    command: 'wsl',
    args: ['bash', '-lc', script],
    cwd: hostPlatform === 'win32' ? projectRoot : undefined,
  };
}

function windowsPathToWsl(value, hostPlatform = process.platform) {
  if (hostPlatform === 'win32') {
    const match = value.match(/^([A-Za-z]):[\\/](.*)$/);
    if (match) {
      return `/mnt/${match[1].toLowerCase()}/${match[2].replace(/\\/g, '/')}`;
    }
  }
  if (!value.startsWith('/')) {
    throw new CodeRabbitAdapterError('wsl_project_path_invalid');
  }
  return value;
}

function renderWslCliPath(value) {
  if (value === '~') return '"$HOME"';
  if (value.startsWith('~/')) {
    return `"$HOME"/${shellQuote(value.slice(2))}`;
  }
  return shellQuote(value);
}

function shellQuote(value) {
  return `'${String(value).replace(/'/g, "'\\''")}'`;
}

function runProcess(invocation, timeoutMs, spawn, env) {
  return new Promise((resolve, reject) => {
    let stdout = '';
    let stderr = '';
    let outputBytes = 0;
    let settled = false;
    const startedAt = Date.now();

    const finish = (callback, value) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      callback(value);
    };

    let child;
    try {
      child = spawn(invocation.command, invocation.args, {
        cwd: invocation.cwd,
        env,
        windowsHide: true,
        stdio: ['ignore', 'pipe', 'pipe'],
      });
    } catch {
      reject(new CodeRabbitAdapterError('cli_process_start_failed'));
      return;
    }

    const timer = setTimeout(() => {
      child.kill();
      finish(reject, new CodeRabbitAdapterError('cli_timeout'));
    }, timeoutMs);

    const append = (current, chunk) => {
      outputBytes += chunk.length;
      if (outputBytes > MAX_OUTPUT_BYTES) {
        child.kill();
        finish(reject, new CodeRabbitAdapterError('cli_output_too_large'));
        return current;
      }
      return current + chunk.toString();
    };

    child.stdout.on('data', (chunk) => {
      stdout = append(stdout, chunk);
    });
    child.stderr.on('data', (chunk) => {
      stderr = append(stderr, chunk);
    });
    child.once('error', (error) => {
      finish(
        reject,
        new CodeRabbitAdapterError(
          error.code === 'ENOENT' ? 'cli_not_found' : 'cli_process_failed',
        ),
      );
    });
    child.once('close', (exitCode) => {
      finish(resolve, {
        exitCode,
        stdout,
        stderr,
        duration: Date.now() - startedAt,
      });
    });
  });
}

function parseSupportedVersion(output) {
  const match = output.match(/\b(\d+)\.(\d+)\.(\d+)(?![\w.+-])/);
  if (!match) return null;

  const [major, minor, patch] = match.slice(1).map(Number);
  if (
    major !== SUPPORTED_MAJOR ||
    minor !== SUPPORTED_MINOR ||
    patch < MIN_SUPPORTED_PATCH
  ) {
    return null;
  }
  return { major, minor, patch, raw: `${major}.${minor}.${patch}` };
}

function isAuthenticated(output) {
  try {
    const status = JSON.parse(output);
    return Boolean(status && !Array.isArray(status) && status.authenticated === true);
  } catch {
    return false;
  }
}

function isFullSha(value) {
  return typeof value === 'string' && /^[a-f\d]{40}$/i.test(value);
}

function parseReviewOutput(output, { expectedDirectory, exitCode }) {
  const events = [];
  for (const line of output.split(/\r?\n/)) {
    if (!line.trim()) continue;
    let event;
    try {
      event = JSON.parse(line);
    } catch {
      throw new CodeRabbitAdapterError('review_output_invalid');
    }
    if (!event || typeof event !== 'object' || Array.isArray(event)) {
      throw new CodeRabbitAdapterError('review_event_invalid');
    }
    events.push(event);
  }

  const contexts = events.filter((event) => event.type === 'review_context');
  const completions = events.filter((event) => event.type === 'complete');
  if (contexts.length !== 1 || completions.length !== 1) {
    throw new CodeRabbitAdapterError('review_completion_invalid');
  }
  if (events[events.length - 1]?.type !== 'complete') {
    throw new CodeRabbitAdapterError('review_completion_not_terminal');
  }
  if (events.some((event) => event.type === 'error')) {
    throw new CodeRabbitAdapterError('review_reported_error');
  }
  if (
    events.some(
      (event) =>
        !['review_context', 'status', 'heartbeat', 'finding', 'complete'].includes(event.type),
    )
  ) {
    throw new CodeRabbitAdapterError('review_event_unknown');
  }

  const reviewContext = contexts[0];
  if (
    typeof reviewContext.workingDirectory !== 'string' ||
    normalizeReviewDirectory(reviewContext.workingDirectory) !==
      normalizeReviewDirectory(expectedDirectory)
  ) {
    throw new CodeRabbitAdapterError('review_scope_unverified');
  }

  const complete = completions[0];
  const gate = complete.gate;
  const unreviewedFileCount = complete.unreviewedFileCount ?? 0;
  const validOutcomes = ['completed', 'completed_with_warnings'];
  if (
    complete.reused === true ||
    (complete.outcome !== undefined && !validOutcomes.includes(complete.outcome)) ||
    !Number.isInteger(unreviewedFileCount) ||
    unreviewedFileCount < 0 ||
    unreviewedFileCount > 0
  ) {
    throw new CodeRabbitAdapterError('review_incomplete');
  }

  if (!['review_completed', 'review_skipped'].includes(complete.status)) {
    throw new CodeRabbitAdapterError('review_completion_invalid');
  }
  if (
    !gate ||
    gate.threshold !== 'critical' ||
    !Number.isInteger(gate.blockingFindings) ||
    !['passed', 'blocked'].includes(gate.status)
  ) {
    throw new CodeRabbitAdapterError('review_gate_unverified');
  }

  const issues = events
    .filter((event) => event.type === 'finding')
    .map(normalizeFinding);
  const criticalCount = issues.filter((issue) => issue.severity === 'CRITICAL').length;
  const completedWithoutFindings =
    complete.status === 'review_skipped' && issues.length === 0;

  if (completedWithoutFindings) {
    if (exitCode !== 0 || gate.status !== 'passed' || gate.blockingFindings !== 0) {
      throw new CodeRabbitAdapterError('review_result_inconsistent');
    }
  } else if (gate.status === 'passed') {
    if (exitCode !== 0 || gate.blockingFindings !== 0 || criticalCount > 0) {
      throw new CodeRabbitAdapterError('review_result_inconsistent');
    }
  } else if (
    exitCode !== 2 ||
    gate.blockingFindings < 1 ||
    gate.blockingFindings !== criticalCount
  ) {
    throw new CodeRabbitAdapterError('review_result_inconsistent');
  }

  return { issues, gate };
}

function normalizeReviewDirectory(value) {
  return path.posix.normalize(value.replace(/\\/g, '/')).replace(/\/$/, '') || '/';
}

function normalizeFinding(event) {
  const severities = {
    critical: 'CRITICAL',
    major: 'HIGH',
    minor: 'MEDIUM',
    trivial: 'LOW',
    info: 'LOW',
    none: 'LOW',
  };
  const severity = severities[event.severity];
  if (!severity || typeof event.fileName !== 'string') {
    throw new CodeRabbitAdapterError('review_finding_invalid');
  }

  return {
    file: event.fileName,
    line: Number.isInteger(event.startLine) ? event.startLine : 0,
    endLine: Number.isInteger(event.endLine) ? event.endLine : 0,
    severity,
    message:
      [event.title, event.comment, event.codegenInstructions].find(
        (value) => typeof value === 'string' && value.trim(),
      ) || 'CodeRabbit finding',
    suggestion: event.suggestions || null,
  };
}

module.exports = {
  CodeRabbitCliAdapter,
  buildInvocation,
  parseReviewOutput,
  windowsPathToWsl,
};
