import { BusinessError } from '@kit.BasicServicesKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { audio } from '@kit.AudioKit';

/**
 * 声音输出实现 - 生成 800Hz 正弦波蜂鸣音
 */
export class SoundOutput {
  private static audioRenderer: audio.AudioRenderer | undefined = undefined;
  private static streamInfo: audio.AudioStreamInfo = {
    samplingRate: audio.AudioSamplingRate.SAMPLE_RATE_44100,
    channels: audio.AudioChannel.CHANNEL_1,
    sampleFormat: audio.AudioSampleFormat.SAMPLE_FORMAT_S16LE,
    encodingType: audio.AudioEncodingType.ENCODING_TYPE_RAW
  }
  private static rendererInfo: audio.AudioRendererInfo = {
    usage: audio.StreamUsage.STREAM_USAGE_MEDIA,
    rendererFlags: 0
  }
  private static rendererOptions: audio.AudioRendererOptions = {
    streamInfo: SoundOutput.streamInfo,
    rendererInfo: SoundOutput.rendererInfo
  }

  static async play(durationMs: number) {
    SoundOutput.stop();
    const bufferSize = SoundOutput.streamInfo.samplingRate * SoundOutput.streamInfo.channels *
      Math.floor(durationMs / 1000);
    const buffer = new ArrayBuffer(bufferSize * 2);
    const data = new Int16Array(buffer);
    const frequency = 800;
    const amplitude = 32767;
    const sampleRate = SoundOutput.streamInfo.samplingRate;
    for (let i = 0; i < bufferSize; i++) {
      data[i] = Math.floor(amplitude * Math.sin(2 * Math.PI * frequency * i / sampleRate));
    }
    try {
      if (SoundOutput.audioRenderer === undefined) {
        SoundOutput.audioRenderer = await audio.createAudioRenderer(SoundOutput.rendererOptions);
        hilog.info(0x0000, 'testTag', 'AudioRenderer created');
      }
      await SoundOutput.audioRenderer!.write(buffer);
      hilog.info(0x0000, 'testTag', 'write buffer success');
    } catch (err) {
      let error = err as BusinessError;
      hilog.error(0x0000, 'testTag', 'write err: ' + JSON.stringify(error));
    }
  }

  static async stop() {
    if (SoundOutput.audioRenderer !== undefined) {
      try {
        await SoundOutput.audioRenderer.stop();
      } catch (err) {
        let error = err as BusinessError;
        hilog.error(0x0000, 'testTag', 'stop err: ' + JSON.stringify(error));
      }
      try {
        await SoundOutput.audioRenderer.release();
      } catch (err) {
        let error = err as BusinessError;
        hilog.error(0x0000, 'testTag', 'release err: ' + JSON.stringify(error));
      }
      SoundOutput.audioRenderer = undefined;
    }
  }
}
