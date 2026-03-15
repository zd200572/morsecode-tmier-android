import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { lightSourceManager } from '@kit.SensorServiceKit';
import { BaseSource } from '@kit.SensorServiceKit';

/**
 * 闪光灯输出实现
 */
export class FlashlightOutput {
  private static lightSourceManager: BaseSource | undefined = undefined;
  private static lightId: string | undefined = undefined;
  private static isOn: boolean = false;

  static async turnOn() {
    try {
      if (FlashlightOutput.lightSourceManager === undefined) {
        FlashlightOutput.lightSourceManager = lightSourceManager.getLightSourceManager();
        hilog.info(0x0000, 'testTag', 'getLightSourceManager success');
      }
      if (FlashlightOutput.lightId === undefined) {
        const lightIdList = await FlashlightOutput.lightSourceManager.getLightIdList();
        if (lightIdList.length === 0) {
          hilog.info(0x0000, 'testTag', 'No light source available.');
          return;
        }
        FlashlightOutput.lightId = lightIdList[0];
      }
      await FlashlightOutput.lightSourceManager.turnOn(FlashlightOutput.lightId);
      FlashlightOutput.isOn = true;
    } catch (error) {
      let err = error as BusinessError;
      hilog.error(0x0000, 'testTag', `turnOn fail, code is ${err.code},  message is ${err.message}`);
    }
  }

  static async turnOff() {
    try {
      if (FlashlightOutput.lightSourceManager === undefined) {
        FlashlightOutput.lightSourceManager = lightSourceManager.getLightSourceManager();
        hilog.info(0x0000, 'testTag', 'getLightSourceManager success');
      }
      if (FlashlightOutput.lightId === undefined) {
        const lightIdList = await FlashlightOutput.lightSourceManager.getLightIdList();
        if (lightIdList.length === 0) {
          hilog.info(0x0000, 'testTag', 'No light source available.');
          return;
        }
        FlashlightOutput.lightId = lightIdList[0];
      }
      await FlashlightOutput.lightSourceManager.turnOff(FlashlightOutput.lightId);
      FlashlightOutput.isOn = false;
    } catch (error) {
      let err = error as BusinessError;
      hilog.error(0x0000, 'testTag', `turnOff fail, code is ${err.code},  message is ${err.message}`);
    }
  }

  static async stop() {
    if (FlashlightOutput.isOn) {
      await FlashlightOutput.turnOff();
    }
  }

  static async isAvailable(): Promise<boolean> {
    try {
      if (FlashlightOutput.lightSourceManager === undefined) {
        FlashlightOutput.lightSourceManager = lightSourceManager.getLightSourceManager();
        hilog.info(0x0000, 'testTag', 'getLightSourceManager success');
      }
      const lightIdList = await FlashlightOutput.lightSourceManager.getLightIdList();
      return lightIdList.length > 0;
    } catch (error) {
      let err = error as BusinessError;
      hilog.error(0x0000, 'testTag', `isAvailable fail, code is ${err.code},  message is ${err.message}`);
      return false;
    }
  }
}
