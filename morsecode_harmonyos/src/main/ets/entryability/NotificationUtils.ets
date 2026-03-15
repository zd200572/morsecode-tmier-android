import { WantAgent } from '@kit.PushKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { notificationManager } from '@kit.NotificationKit';
import { WantAgentInfo } from './NotificationUtils';

export default class TriggerNotification {
  let notificationRequest: notificationManager.NotificationRequest = {
    id: 1,
    content: {
      notificationContentType: notificationManager.NotificationContentType.NOTIFICATION_CONTENT_BASIC_TEXT,
      normal: {
        title: 'test_title',
        text: 'test_text',
        additionalText: 'test_additionalText',
      }
    },
    notificationSlotType: notificationManager.SlotType.SOCIAL_COMMUNICATION
  };

  try {
    notificationManager.publish(notificationRequest).then(() => { // publish完成回调
      hilog.info(0x0000, 'testTag', 'publish success');
    }).catch((err: BusinessError) => {
      hilog.error(0x0000, 'testTag', `publish fail, err: ${JSON.stringify(err)}`);
    });
  } catch (err) {
    hilog.error(0x0000, 'testTag', `publish err, err: ${JSON.stringify(err)}`);
  }

  let wantAgentInfo: WantAgent.WantAgentInfo = WantAgentInfo;
  try {
    WantAgent.getWantAgent(wantAgentInfo).then((wantAgent: WantAgent.WantAgent) => {
      hilog.info(0x0000, 'testTag', 'getWantAgent success');
      triggerNotification(wantAgent);
    }).catch((err: BusinessError) => {
      hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
    });
  } catch (err) {
    hilog.error(0x0000, 'testTag', 'getWantAgent failed! err:' + JSON.stringify(err));
  }

  try {
    triggerNotification(wantAgent, (err: BusinessError, data: void) => {
      if (err) {
        hilog.error(0x0000, 'testTag', 'triggerNotification failed! err:' + JSON.stringify(err));
      } else {
        hilog.info(0x0000, 'testTag', 'triggerNotification success');
      }
    });
  } catch (err) {
    hilog.error(0x0000, 'testTag', 'triggerNotification failed! err:' + JSON.stringify(err));
  }
}
