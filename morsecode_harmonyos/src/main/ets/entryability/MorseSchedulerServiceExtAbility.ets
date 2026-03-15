import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { ServiceExtensionAbility, Want } from '@kit.AbilityKit';
import { WantAgent, triggerNotification, cancelNotification } from '@kit.PushKit';
import { SignalPlayer } from '../utils/SignalPlayer';
import { MorseCodeEngine } from '../utils/MorseCodeEngine';
import { VibrationOutput } from '../utils/VibrationOutput';
import { SoundOutput } from '../utils/SoundOutput';
import { FlashlightOutput } from '../utils/LightSourceManager';
import { WantAgentInfo, getNotification } from './NotificationUtils';

export default class MorseSchedulerServiceExtAbility extends ServiceExtensionAbility {
  onCreate(want: Want) {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onCreate');
  }

  onDestroy() {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onDestroy');
  }

  onRequest(want: Want, startId: number) {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onRequest');
    try {
      WantAgent.getWantAgent(WantAgentInfo).then((wantAgent: WantAgent.WantAgent) => {
        triggerNotification(wantAgent).then(() => {
          hilog.info(0x0000, 'testTag', 'Notification triggered');
        });
      }).catch((err: BusinessError) => {
        hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
      });
    } catch (err) {
      hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
    }
  }

  onRequest(want: Want, startId: number) {
    hilog.info(0x0000, 'testTag', '%{public}s', 'Ability onRequest');
    let signalPlayer = new SignalPlayer();
    signalPlayer.enableVibration = true;
    signalPlayer.enableSound = true;
    signalPlayer.enableFlashlight = false;
    let includeDate = false;
    const date = new Date();
    let year = date.getFullYear();
    let month = date.getMonth() + 1;
    let day = date.getDate();
    let hour = date.getHours();
    let minute = date.getMinutes();
    try {
      let wantAgentInfo: WantAgent.WantAgentInfo = WantAgentInfo;
      WantAgent.getWantAgent(wantAgentInfo).then((wantAgent: WantAgent.WantAgent) => {
        triggerNotification(wantAgent).then(() => {
          hilog.info(0x0000, 'testTag', 'Notification triggered');
        });
      }).catch((err: BusinessError) => {
        hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
      });
    } catch (err) {
      hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
    }
    signalPlayer.playWithDate(year, month, day, hour, minute, includeDate);
  }

  getNotification(want: Want) {
    let notificationRequest = getNotification();
    notificationRequest.id = 1;
    notificationRequest.content = {
      notificationContentType: notificationManager.NotificationContentType.NOTIFICATION_CONTENT_BASIC_TEXT,
      normal: {
        title: '测试标题',
        text: '测试内容',
      }
    }
    notificationManager.publish(notificationRequest, (err: BusinessError) => {
      if (err) {
        hilog.error(0x0000, 'testTag', `Failed to publish notification. Code: ${err.code}, message: ${err.message}`);
        return;
      }
      hilog.info(0x0000, 'testTag', 'Success to publish notification.');
    });
  }
}
