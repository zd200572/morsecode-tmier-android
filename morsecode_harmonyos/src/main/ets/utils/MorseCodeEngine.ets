/**
 * Morse 电码核心引擎
 * 将时间和日期编码为 Morse 信号序列
 */
export class MorseCodeEngine {

  /** 信号类型 */
  static SignalType: SignalType = SignalType;
  /** 单个信号单元 */
  static Signal: Signal = Signal;

  /** 数字 0-9 的 Morse 编码 */
  private static MORSE_TABLE: Record<string, string> = {
    '0': "-----",
    '1': ".----",
    '2': "..---",
    '3': "...--",
    '4': "....-",
    '5': ".....",
    '6': "-....",
    '7': "--...",
    '8': "---..",
    '9': "----."
  };

  /** 时间参数 (ms) */
  static DOT_DURATION = 200;
  static DASH_DURATION = 600;
  static SYMBOL_GAP = 200;    // 符号间隔
  static CHAR_GAP = 600;      // 字符间隔
  static GROUP_GAP = 1200;    // 时/分 分隔
  static DATE_TIME_GAP = 2000; // 日期与时间分隔

  /**
   * 将时间编码为 Morse 字符串
   * @return Pair<小时Morse, 分钟Morse>，每个数字之间用空格分隔
   */
  static encodeTime(hour: number, minute: number): [string, string] {
    const hourStr = String(hour).padStart(2, '0');
    const minuteStr = String(minute).padStart(2, '0');

    const hourMorse = hourStr.split('').map((digit: string) => MorseCodeEngine.MORSE_TABLE[digit] ?? "").join(' ');
    const minuteMorse = minuteStr.split('').map((digit: string) => MorseCodeEngine.MORSE_TABLE[digit] ?? "").join(' ');

    return [hourMorse, minuteMorse];
  }

  /**
   * 将日期编码为 Morse 字符串
   * @return Triple<年Morse, 月Morse, 日Morse>
   */
  static encodeDate(year: number, month: number, day: number): [string, string, string] {
    const yearStr = String(year).padStart(4, '0');
    const monthStr = String(month).padStart(2, '0');
    const dayStr = String(day).padStart(2, '0');

    const yearMorse = yearStr.split('').map((digit: string) => MorseCodeEngine.MORSE_TABLE[digit] ?? "").join(' ');
    const monthMorse = monthStr.split('').map((digit: string) => MorseCodeEngine.MORSE_TABLE[digit] ?? "").join(' ');
    const dayMorse = dayStr.split('').map((digit: string) => MorseCodeEngine.MORSE_TABLE[digit] ?? "").join(' ');

    return [yearMorse, monthMorse, dayMorse];
  }

  /**
   * 将 Morse 字符串转换为可视化用的符号列表
   * @return 列表元素: '.' = 短信号, '-' = 长信号, ' ' = 字符间隔, '|' = 时/分分隔
   */
  static toSymbolList(hourMorse: string, minuteMorse: string): string[] {
    const symbols: string[] = [];
    for (const ch of hourMorse) {
      symbols.push(ch);
    }
    symbols.push('|');
    for (const ch of minuteMorse) {
      symbols.push(ch);
    }
    return symbols;
  }

  /**
   * 将时/分 Morse 编码生成信号序列
   */
  static generateSignalSequence(hour: number, minute: number): Signal[] {
    const [hourMorse, minuteMorse] = MorseCodeEngine.encodeTime(hour, minute);
    const signals: Signal[] = [];

    const addMorseString = (morse: string) => {
      let i = 0;
      while (i < morse.length) {
        switch (morse[i]) {
          case '.':
            signals.push(new Signal(SignalType.ON, MorseCodeEngine.DOT_DURATION));
            // 下一个若还是符号，加符号间隔
            if (i + 1 < morse.length && morse[i + 1] !== ' ') {
              signals.push(new Signal(SignalType.OFF, MorseCodeEngine.SYMBOL_GAP));
            }
            break;
          case '-':
            signals.push(new Signal(SignalType.ON, MorseCodeEngine.DASH_DURATION));
            if (i + 1 < morse.length && morse[i + 1] !== ' ') {
              signals.push(new Signal(SignalType.OFF, MorseCodeEngine.SYMBOL_GAP));
            }
            break;
          case ' ':
            signals.push(new Signal(SignalType.OFF, MorseCodeEngine.CHAR_GAP));
            break;
        }
        i++;
      }
    };

    addMorseString(hourMorse);
    signals.push(new Signal(SignalType.OFF, MorseCodeEngine.GROUP_GAP));
    addMorseString(minuteMorse);

    return signals;
  }

  /**
   * 将日期和时间生成信号序列
   * @param includeDate 是否包含日期播报
   */
  static generateSignalSequenceWithDate(
    year: number, month: number, day: number,
    hour: number, minute: number,
    includeDate: boolean
  ): Signal[] {
    const signals: Signal[] = [];

    const addMorseString = (morse: string) => {
      let i = 0;
      while (i < morse.length) {
        switch (morse[i]) {
          case '.':
            signals.push(new Signal(SignalType.ON, MorseCodeEngine.DOT_DURATION));
            if (i + 1 < morse.length && morse[i + 1] !== ' ') {
              signals.push(new Signal(SignalType.OFF, MorseCodeEngine.SYMBOL_GAP));
            }
            break;
          case '-':
            signals.push(new Signal(SignalType.ON, MorseCodeEngine.DASH_DURATION));
            if (i + 1 < morse.length && morse[i + 1] !== ' ') {
              signals.push(new Signal(SignalType.OFF, MorseCodeEngine.SYMBOL_GAP));
            }
            break;
          case ' ':
            signals.push(new Signal(SignalType.OFF, MorseCodeEngine.CHAR_GAP));
            break;
        }
        i++;
      }
    };

    // 先播报日期（如果启用）
    if (includeDate) {
      const [yearMorse, monthMorse, dayMorse] = MorseCodeEngine.encodeDate(year, month, day);
      addMorseString(yearMorse);
      signals.push(new Signal(SignalType.OFF, MorseCodeEngine.GROUP_GAP));
      addMorseString(monthMorse);
      signals.push(new Signal(SignalType.OFF, MorseCodeEngine.GROUP_GAP));
      addMorseString(dayMorse);
      signals.push(new Signal(SignalType.OFF, MorseCodeEngine.DATE_TIME_GAP));
    }

    // 播报时间
    const [hourMorse, minuteMorse] = MorseCodeEngine.encodeTime(hour, minute);
    addMorseString(hourMorse);
    signals.push(new Signal(SignalType.OFF, MorseCodeEngine.GROUP_GAP));
    addMorseString(minuteMorse);

    return signals;
  }

  /**
   * 获取 Morse 字符串中用于 UI 显示的符号列表（不含空格）
   * 每个元素代表一个 dot 或 dash
   */
  static getDisplaySymbols(hour: number, minute: number): MorseSymbol[] {
    const [hourMorse, minuteMorse] = MorseCodeEngine.encodeTime(hour, minute);
    const symbols: MorseSymbol[] = [];
    let globalIdx = 0;

    const parseMorse = (morse: string, groupStart: number) => {
      let charIdx = groupStart;
      for (const ch of morse) {
        switch (ch) {
          case '.':
            symbols.push(new MorseSymbol(false, charIdx, globalIdx++));
            break;
          case '-':
            symbols.push(new MorseSymbol(true, charIdx, globalIdx++));
            break;
          case ' ':
            charIdx++;
            break;
        }
      }
    };

    parseMorse(hourMorse, 0);
    parseMorse(minuteMorse, 2);

    return symbols;
  }

  /**
   * 获取日期和时间显示用的符号列表
   */
  static getDisplaySymbolsWithDate(
    year: number, month: number, day: number,
    hour: number, minute: number,
    includeDate: boolean
  ): MorseSymbol[] {
    const symbols: MorseSymbol[] = [];
    let globalIdx = 0;

    const parseMorse = (morse: string, groupStart: number) => {
      let charIdx = groupStart;
      for (const ch of morse) {
        switch (ch) {
          case '.':
            symbols.push(new MorseSymbol(false, charIdx, globalIdx++));
            break;
          case '-':
            symbols.push(new MorseSymbol(true, charIdx, globalIdx++));
            break;
          case ' ':
            charIdx++;
            break;
        }
      }
    };

    let currentGroup = 0;

    if (includeDate) {
      const [yearMorse, monthMorse, dayMorse] = MorseCodeEngine.encodeDate(year, month, day);
      parseMorse(yearMorse, currentGroup);
      currentGroup += 4; // 年有4位数字
      parseMorse(monthMorse, currentGroup);
      currentGroup += 2;
      parseMorse(dayMorse, currentGroup);
      currentGroup += 2;
    }

    const [hourMorse, minuteMorse] = MorseCodeEngine.encodeTime(hour, minute);
    parseMorse(hourMorse, currentGroup);
    parseMorse(minuteMorse, currentGroup + 2);

    return symbols;
  }
}

/**
 * 信号类型
 */
export enum SignalType {
  /** 信号开启 */
  ON = 1,
  /** 信号关闭 */
  OFF = 0
}

/**
 * 单个信号单元
 */
export class Signal {
  type: SignalType;
  durationMs: number;

  constructor(type: SignalType, durationMs: number) {
    this.type = type;
    this.durationMs = durationMs;
  }
}

/**
 * Morse符号
 */
export class MorseSymbol {
  isDash: boolean;
  groupIndex: number;
  symbolIndex: number;

  constructor(isDash: boolean, groupIndex: number, symbolIndex: number) {
    this.isDash = isDash;
    this.groupIndex = groupIndex;
    this.symbolIndex = symbolIndex;
  }
}
