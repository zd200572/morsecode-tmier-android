import { promptAction } from '@kit.ArkUI';

export class ToastUtils {
  static showToast(message: string) {
    promptAction.showToast({
      message: message,
      duration: 2000,
    });
  }
}
