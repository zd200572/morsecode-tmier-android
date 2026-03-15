import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { vibrator } from '@kit.SensorServiceKit';

/**
 * 振动输出实现
 */
export class VibrationOutput {
  /**
   * 振动
   * @param durationMs 振动时长
   */
  static vibrate(durationMs: number) {
    try {
      vibrator.startVibration({
        type: 'time',
        duration: durationMs
      }, {
        id: 0,
        usage: 'alarm'
      }, (error: BusinessError) => {
        if (error) {
          hilog.error(0x0000, 'testTag', `Vibration error. Code: ${error.code}, message: ${error.message}`);
          return;
        }
      });
    } catch (err) {
      let error = err as BusinessError;
      hilog.error(0x0000, 'testTag', `Vibration error. Code: ${error.code}, message: ${error.message}`);
    }
  }

  static stop() {
    try {
      vibrator.stopVibration();
    } catch (error) {
      let err = error as BusinessError;
      hilog.error(0x0000, 'testTag', `stopVibration fail, code is ${err.code},  message is ${err.message}`);
    }
  }
}
