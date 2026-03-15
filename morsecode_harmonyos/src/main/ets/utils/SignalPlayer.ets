import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { MorseCodeEngine, Signal } from './MorseCodeEngine';
import { VibrationOutput } from './VibrationOutput';
import { SoundOutput } from './SoundOutput';
import { FlashlightOutput } from './LightSourceManager';

/**
 * 统一信号播放控制器
 * 协调振动、声音和闪光灯三种输出
 */
export class SignalPlayer {
  enableVibration: boolean = true;
  enableSound: boolean = true;
  enableFlashlight: boolean = false;

  private isPlaying: boolean = false;
  private playTask: number = -1;

  /** 播放进度回调：当前信号索引，总信号数 */
  onProgress: ((currentIndex: number, total: number) => void) | undefined = undefined;
  onComplete: (() => void) | undefined = undefined;

  /**
   * 播放指定时间的 Morse 信号序列（仅时间）
   */
  play(hour: number, minute: number) {
    SignalPlayer.playWithDate(0, 0, 0, hour, minute, false);
  }

  /**
   * 播放日期和时间的 Morse 信号序列
   * @param includeDate 是否包含日期播报
   */
  playWithDate(
    year: number, month: number, day: number,
    hour: number, minute: number,
    includeDate: boolean
  ) {
    if (SignalPlayer.isPlaying) {
      return;
    }
    const signals = MorseCodeEngine.generateSignalSequenceWithDate(
      year, month, day, hour, minute, includeDate
    );
    SignalPlayer.isPlaying = true;

    SignalPlayer.playTask = setTimeout(() => {
      try {
        let signalIdx = 0;
        for (const [index, signal] of signals.entries()) {
          if (signal.type === MorseCodeEngine.SignalType.ON) {
            SignalPlayer.onProgress?.(signalIdx, signals.filter(s => s.type === MorseCodeEngine.SignalType.ON).length);
            signalIdx++;
            // 同时启动所有已启用的输出
            if (SignalPlayer.enableVibration) {
              VibrationOutput.vibrate(signal.durationMs);
            }
            if (SignalPlayer.enableSound) {
              SoundOutput.play(signal.durationMs);
            }
            if (SignalPlayer.enableFlashlight) {
              FlashlightOutput.turnOn();
            }
          } else {
            // OFF 信号
            if (SignalPlayer.enableFlashlight) {
              FlashlightOutput.turnOff();
            }
          }
        }
      } finally {
        SignalPlayer.stopOutputs();
        SignalPlayer.isPlaying = false;
        SignalPlayer.onComplete?.();
      }
    }, 1000);
  }

  stop() {
    clearTimeout(SignalPlayer.playTask);
    SignalPlayer.playTask = -1;
    SignalPlayer.stopOutputs();
    SignalPlayer.isPlaying = false;
  }

  private static stopOutputs() {
    VibrationOutput.stop();
    SoundOutput.stop();
    FlashlightOutput.stop();
  }

  release() {
    SignalPlayer.stop();
    SoundOutput.stop();
  }
}
