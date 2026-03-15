import { hilog } from '@kit.PerformanceAnalysisKit';

class Logger {
  private domain: number;
  private prefix: string;

  constructor(prefix: string) {
    this.prefix = prefix;
    this.domain = 0xFF00;
  }

  debug(...args: Object[]): void {
    hilog.debug(this.domain, this.prefix, '%{public}s', args.join(' '));
  }

  info(...args: Object[]): void {
    hilog.info(this.domain, this.prefix, '%{public}s', args.join(' '));
  }

  warn(...args: Object[]): void {
    hilog.warn(this.domain, this.prefix, '%{public}s', args.join(' '));
  }

  error(...args: Object[]): void {
    hilog.error(this.domain, this.prefix, '%{public}s', args.join(' '));
  }
}

export default new Logger('[MorseTime]');
