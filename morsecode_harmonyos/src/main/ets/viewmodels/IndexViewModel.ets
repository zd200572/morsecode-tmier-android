import { preferences } from '@kit.ArkData';
import { BusinessError } from '@kit.BasicServicesKit';
import { MorseCodeEngine, MorseSymbol } from '../utils/MorseCodeEngine';
import { SignalPlayer } from '../utils/SignalPlayer';
import { VibrationOutput } from '../utils/VibrationOutput';
import { SoundOutput } from '../utils/SoundOutput';
import { FlashlightOutput } from '../utils/LightSourceManager';
import { DateUtils } from '../common/utils/DateUtils';
import { TimeUtils } from '../common/utils/TimeUtils';
import { Constants } from '../common/constants/Constants';
import Logger from '../common/utils/Logger';

export class IndexViewModel {
  private static instance: IndexViewModel;
  private static dataStore: preferences.Preferences = preferences.getPreferencesSync(getContext(getContext()), { name: 'morseTimeStore' });

  private constructor() {
  }

  public static getInstance(): IndexViewModel {
    if (!IndexViewModel.instance) {
      IndexViewModel.instance = new IndexViewModel();
    }
    return IndexViewModel.instance;
  }

  /**
   * 格式化时间显示
   * @param hour 小时
   * @param minute 分钟
   * @returns 格式化后的时间字符串，例如 "12 : 34"
   */
  formatTime(hour: number, minute: number): string {
    return TimeUtils.formatTime(hour, minute);
  }

  /**
   * 格式化秒数显示
   * @param second 秒数
   * @returns 格式化后的秒数字符串，例如 "05"
   */
  formatSecond(second: number): string {
    return TimeUtils.formatSecond(second);
  }

  /**
   * 获取当前时间
   * @returns 当前时间的年、月、日、时、分、秒
   */
  getCurrentTime(): [number, number, number, number, number, number] {
    return DateUtils.getCurrentTime();
  }

  /**
   * 获取Morse码字符串
   * @param hour 小时
   * @param minute 分钟
   * @returns 小时和分钟的Morse码字符串
   */
  getMorseCode(hour: number, minute: number): [string, string] {
    return MorseCodeEngine.encodeTime(hour, minute);
  }

  /**
   * 获取Morse码符号
   * @param hour 小时
   * @param minute 分钟
   * @returns Morse码符号列表
   */
  getMorseSymbol(hour: number, minute: number): MorseSymbol[] {
    return MorseCodeEngine.getDisplaySymbols(hour, minute);
  }

  /**
   * 播放Morse码
   * @param enableVibration 是否启用振动
   * @param enableSound 是否启用声音
   * @param enableFlashlight 是否启用闪光灯
   * @param includeDate 是否包含日期
   * @param signalPlayer 信号播放器
   */
  playMorseCode(enableVibration: boolean, enableSound: boolean, enableFlashlight: boolean, includeDate: boolean,
    signalPlayer: SignalPlayer): void {
    let [year, month, day, hour, minute] = this.getCurrentTime();
    signalPlayer.enableVibration = enableVibration;
    signalPlayer.enableSound = enableSound;
    signalPlayer.enableFlashlight = enableFlashlight;
    signalPlayer.playWithDate(year, month, day, hour, minute, includeDate);
  }

  /**
   * 停止播放Morse码
   * @param signalPlayer 信号播放器
   */
  stopMorseCode(signalPlayer: SignalPlayer): void {
    signalPlayer.stop();
  }

  /**
   * 设置是否启用振动
   * @param value 是否启用振动
   */
  setEnableVibration(value: boolean): void {
    try {
      IndexViewModel.dataStore.putSync(Constants.KEY_VIBRATION, value);
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to put data. Code: ${error.code}, message: ${error.message}`);
    }
  }

  /**
   * 设置是否启用声音
   * @param value 是否启用声音
   */
  setEnableSound(value: boolean): void {
    try {
      IndexViewModel.dataStore.putSync(Constants.KEY_SOUND, value);
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to put data. Code: ${error.code}, message: ${error.message}`);
    }
  }

  /**
   * 设置是否启用闪光灯
   * @param value 是否启用闪光灯
   */
  setEnableFlashlight(value: boolean): void {
    try {
      IndexViewModel.dataStore.putSync(Constants.KEY_FLASHLIGHT, value);
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to put data. Code: ${error.code}, message: ${error.message}`);
    }
  }

  /**
   * 设置是否包含日期
   * @param value 是否包含日期
   */
  setIncludeDate(value: boolean): void {
    try {
      IndexViewModel.dataStore.putSync(Constants.KEY_INCLUDE_DATE, value);
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to put data. Code: ${error.code}, message: ${error.message}`);
    }
  }

  /**
   * 设置定时播报间隔
   * @param value 定时播报间隔
   */
  setInterval(value: number): void {
    try {
      IndexViewModel.dataStore.putSync(Constants.KEY_INTERVAL, value);
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to put data. Code: ${error.code}, message: ${error.message}`);
    }
  }

  /**
   * 获取是否启用振动
   * @returns 是否启用振动
   */
  getEnableVibration(): boolean {
    let value = false;
    try {
      value = IndexViewModel.dataStore.getSync(Constants.KEY_VIBRATION, Constants.DEFAULT_VIBRATION) as boolean;
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to get data. Code: ${error.code}, message: ${error.message}`);
    }
    return value;
  }

  /**
   * 获取是否启用声音
   * @returns 是否启用声音
   */
  getEnableSound(): boolean {
    let value = false;
    try {
      value = IndexViewModel.dataStore.getSync(Constants.KEY_SOUND, Constants.DEFAULT_SOUND) as boolean;
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to get data. Code: ${error.code}, message: ${error.message}`);
    }
    return value;
  }

  /**
   * 获取是否启用闪光灯
   * @returns 是否启用闪光灯
   */
  getEnableFlashlight(): boolean {
    let value = false;
    try {
      value = IndexViewModel.dataStore.getSync(Constants.KEY_FLASHLIGHT, Constants.DEFAULT_FLASHLIGHT) as boolean;
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to get data. Code: ${error.code}, message: ${error.message}`);
    }
    return value;
  }

  /**
   * 获取是否包含日期
   * @returns 是否包含日期
   */
  getIncludeDate(): boolean {
    let value = false;
    try {
      value = IndexViewModel.dataStore.getSync(Constants.KEY_INCLUDE_DATE, Constants.DEFAULT_INCLUDE_DATE) as boolean;
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to get data. Code: ${error.code}, message: ${error.message}`);
    }
    return value;
  }

  /**
   * 获取定时播报间隔
   * @returns 定时播报间隔
   */
  getInterval(): number {
    let value = 0;
    try {
      value = IndexViewModel.dataStore.getSync(Constants.KEY_INTERVAL, Constants.DEFAULT_INTERVAL) as number;
    } catch (err) {
      let error = err as BusinessError;
      Logger.error(`Failed to get data. Code: ${error.code}, message: ${error.message}`);
    }
    return value;
  }
}
