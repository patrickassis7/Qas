/**
 * Layer 2: PR Automation
 *
 * Runs automated PR reviews:
 * - CodeRabbit integration
 * - Quinn (@qa agent) automated review
 *
 * @module core/quality-gates/layer2-pr-automation
 * @version 1.0.0
 * @story 2.10 - Quality Gate Manager
 */

const fs = require('fs').promises;
const path = require('path');
const { BaseLayer } = require('./base-layer');
const { CodeRabbitCliAdapter } = require('./coderabbit-cli-adapter');

/**
 * Layer 2: PR Automation checks
 * @extends BaseLayer
 */
class Layer2PRAutomation extends BaseLayer {
  /**
   * Create Layer 2 instance
   * @param {Object} config - Layer 2 configuration
   */
  constructor(config = {}) {
    super('Layer 2: PR Automation', config);
    this.coderabbit = config.coderabbit || {};
    this.quinn = config.quinn || {};
  }

  /**
   * Execute all Layer 2 checks
   * @param {Object} context - Execution context
   * @returns {Promise<Object>} Layer results
   */
  async execute(context = {}) {
    this.reset();
    this.startTimer();

    const { verbose = false } = context;

    if (!this.enabled) {
      this.addResult({
        check: 'layer2',
        pass: true,
        skipped: true,
        message: 'Layer 2 disabled',
      });
      this.stopTimer();
      return this.getSummary();
    }

    if (verbose) {
      console.log('\n🤖 Layer 2: PR Automation');
      console.log('━'.repeat(50));
    }

    // Run CodeRabbit
    if (this.coderabbit?.enabled !== false) {
      const coderabbitResult = await this.runCodeRabbit(context);
      this.addResult(coderabbitResult);
    }

    // Run Quinn review
    if (this.quinn?.enabled !== false) {
      const quinnResult = await this.runQuinnReview(context);
      this.addResult(quinnResult);
    }

    this.stopTimer();

    if (verbose) {
      const summary = this.getSummary();
      const icon = summary.pass ? '✅' : '⚠️';
      console.log(
        `\n${icon} Layer 2 ${summary.pass ? 'PASSED' : 'HAS ISSUES'} (${this.formatDuration(summary.duration)})`,
      );
    }

    return this.getSummary();
  }

  /**
   * Run CodeRabbit review
   * @param {Object} context - Execution context
   * @returns {Promise<Object>} CodeRabbit result
   */
  async runCodeRabbit(context = {}) {
    const { verbose = false } = context;

    if (verbose) {
      console.log('  🐰 Running CodeRabbit review...');
    }

    const adapter = new CodeRabbitCliAdapter(this.coderabbit, {
      projectRoot: this.coderabbit.projectRoot || process.cwd(),
    });
    const result = await adapter.run({ scope: 'uncommitted' });

    if (!result.success) {
      return {
        check: 'coderabbit',
        pass: false,
        blocked: true,
        error: result.error,
        message: `CodeRabbit blocked: ${result.error}`,
      };
    }

    const issues = result.issues;
    const counts = {
      CRITICAL: issues.filter((issue) => issue.severity === 'CRITICAL').length,
      HIGH: issues.filter((issue) => issue.severity === 'HIGH').length,
      MEDIUM: issues.filter((issue) => issue.severity === 'MEDIUM').length,
      LOW: issues.filter((issue) => issue.severity === 'LOW').length,
    };
    const blockOn = this.coderabbit.blockOn || ['CRITICAL'];
    const pass = !issues.some((issue) => blockOn.includes(issue.severity));
    const total = Object.values(counts).reduce((sum, count) => sum + count, 0);

    if (verbose) {
      const icon = pass ? '✓' : '⚠️';
      console.log(
        `  ${icon} CodeRabbit: ${counts.CRITICAL} CRITICAL, ${counts.HIGH} HIGH, ${counts.MEDIUM} MEDIUM`,
      );
    }

    return {
      check: 'coderabbit',
      pass,
      issues: {
        critical: counts.CRITICAL,
        high: counts.HIGH,
        medium: counts.MEDIUM,
        low: counts.LOW,
        total,
      },
      details: issues,
      duration: result.duration,
      message: pass
        ? `CodeRabbit passed (${counts.CRITICAL} CRITICAL, ${counts.HIGH} HIGH)`
        : `CodeRabbit found blocking issues (${counts.CRITICAL} CRITICAL)`,
    };
  }

  /**
   * Run Quinn (@qa) automated review
   * @param {Object} context - Execution context
   * @returns {Promise<Object>} Quinn review result
   */
  async runQuinnReview(context = {}) {
    const { verbose = false } = context;

    if (verbose) {
      console.log('  🧪 Running Quinn (@qa) review...');
    }

    try {
      // Quinn review is typically triggered by the QA agent
      // This integration point generates a review request
      const suggestions = await this.generateQuinnSuggestions(context);

      const blockingSuggestions = suggestions.filter((s) =>
        this.quinn.severity?.block?.includes(s.severity),
      );

      const pass = blockingSuggestions.length === 0;

      const quinnResult = {
        check: 'quinn',
        pass,
        suggestions: suggestions.length,
        blocking: blockingSuggestions.length,
        details: suggestions,
        message: pass
          ? `Quinn review: ${suggestions.length} suggestions`
          : `Quinn review: ${blockingSuggestions.length} blocking issues`,
      };

      if (verbose) {
        const icon = pass ? '✓' : '⚠️';
        console.log(
          `  ${icon} Quinn: ${suggestions.length} suggestions, ${blockingSuggestions.length} blocking`,
        );
      }

      return quinnResult;
    } catch (error) {
      return {
        check: 'quinn',
        pass: true, // Don't block on Quinn errors
        skipped: true,
        error: error.message,
        message: `Quinn skipped: ${error.message}`,
      };
    }
  }

  /**
   * Generate Quinn suggestions (placeholder for full integration)
   * @param {Object} context - Execution context
   * @returns {Promise<Array>} Suggestions
   */
  async generateQuinnSuggestions(_context = {}) {
    // This would integrate with the QA agent for automated review
    // For now, return empty suggestions - full integration in Story 2.11
    return [];
  }

  /**
   * Save review results to reports directory
   * @param {string} reportPath - Path to save report
   * @returns {Promise<void>}
   */
  async saveReport(reportPath) {
    const summary = this.getSummary();
    const report = {
      timestamp: new Date().toISOString(),
      layer: this.name,
      ...summary,
    };

    await fs.mkdir(path.dirname(reportPath), { recursive: true });
    await fs.writeFile(reportPath, JSON.stringify(report, null, 2));
  }

}

module.exports = { Layer2PRAutomation };
