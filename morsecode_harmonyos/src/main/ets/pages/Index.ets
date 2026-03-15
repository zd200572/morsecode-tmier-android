import { IndexViewModel } from '../viewmodels/IndexViewModel';
import { SignalPlayer } from '../utils/SignalPlayer';
import { MorseSymbol } from '../utils/MorseCodeEngine';
import { FlashlightOutput } from '../utils/LightSourceManager';
import { Constants } from '../common/constants/Constants';
import Logger from '../common/utils/Logger';
import { ToastUtils } from '../common/utils/ToastUtils';

@Entry
@Component
export struct Index {
  @State currentTime: [number, number, number, number, number, number] = [0, 0, 0, 0, 0, 0];
  @State morseCode: [string, string] = ['', ''];
  @State morseSymbol: MorseSymbol[] = [];
  @State enableVibration: boolean = true;
  @State enableSound: boolean = true;
  @State enableFlashlight: boolean = false;
  @State includeDate: boolean = false;
  @State interval: number = Constants.INTERVAL_60;
  @State isPlaying: boolean = false;
  @State playIndex: number = -1;
  @State totalSignals: number = 0;
  @State hasFlashlight: boolean = false;
  @State isScheduled: boolean = false;
  private viewModel: IndexViewModel = IndexViewModel.getInstance();
  private signalPlayer: SignalPlayer = new SignalPlayer();
  private timer: number = -1;

  aboutToAppear(): void {
    this.enableVibration = this.viewModel.getEnableVibration();
    this.enableSound = this.viewModel.getEnableSound();
    this.enableFlashlight = this.viewModel.getEnableFlashlight();
    this.includeDate = this.viewModel.getIncludeDate();
    this.interval = this.viewModel.getInterval();
    this.signalPlayer.onProgress = (current: number, total: number) => {
      this.playIndex = current;
      this.totalSignals = total;
    };
    this.signalPlayer.onComplete = () => {
      this.isPlaying = false;
      this.playIndex = -1;
    };
    this.updateTime();
    this.timer = setInterval(() => {
      this.updateTime();
    }, 1000);
    FlashlightOutput.isAvailable().then((value: boolean) => {
      this.hasFlashlight = value;
    });
  }

  aboutToDisappear(): void {
    if (this.timer !== -1) {
      clearInterval(this.timer);
      this.timer = -1;
    }
  }

  updateTime() {
    this.currentTime = this.viewModel.getCurrentTime();
    this.morseCode = this.viewModel.getMorseCode(this.currentTime[TimeConstants.HOUR], this.currentTime[TimeConstants.MINUTE]);
    this.morseSymbol = this.viewModel.getMorseSymbol(this.currentTime[TimeConstants.HOUR], this.currentTime[TimeConstants.MINUTE]);
  }

  build() {
    RelativeContainer() {
      Text(this.viewModel.formatTime(this.currentTime[TimeConstants.HOUR], this.currentTime[TimeConstants.MINUTE]))
        .fontSize(64)
        .fontWeight(FontWeight.Thin)
        .fontColor('#FFB300')
        .margin({ top: 20 })
        .id('MorseTimeTitle')
        .alignRules({
          center: { anchor: '__container__', align: VerticalAlign.Top },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
      Text(this.viewModel.formatSecond(this.currentTime[TimeConstants.SECOND]))
        .fontSize(16)
        .fontColor('#666666')
        .margin({ top: 10 })
        .id('MorseTimeSecond')
        .alignRules({
          top: { anchor: 'MorseTimeTitle', align: VerticalAlign.Bottom }
        })
      Row() {
        Text('H  ')
          .fontSize(16)
          .fontColor('#666666')
        Text(this.morseCode[0].replace('.', '·').replace('-', '−'))
          .fontSize(22)
          .fontWeight(FontWeight.Light)
          .fontColor('#B0B0B0')
          .letterSpacing(3)
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding(20)
      .borderRadius(16)
      .backgroundColor('#1E1E1E')
      .margin({ top: 16 })
      .id('MorseCodeHour')
      .alignRules({
        top: { anchor: 'MorseTimeSecond', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row() {
        Text('M  ')
          .fontSize(16)
          .fontColor('#666666')
        Text(this.morseCode[1].replace('.', '·').replace('-', '−'))
          .fontSize(22)
          .fontWeight(FontWeight.Light)
          .fontColor('#B0B0B0')
          .letterSpacing(3)
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .padding(20)
      .borderRadius(16)
      .backgroundColor('#1E1E1E')
      .margin({ top: 6 })
      .id('MorseCodeMinute')
      .alignRules({
        top: { anchor: 'MorseCodeHour', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row() {
        ForEach(this.morseSymbol.filter(m => m.groupIndex < 2), (item: MorseSymbol) => {
          if (item.isDash) {
            Row()
              .width(24)
              .height(8)
              .backgroundColor(this.playIndex === item.symbolIndex && this.isPlaying ? '#FFB300' :
                this.playIndex > item.symbolIndex && this.isPlaying ? '#66FFB300' : '#3A3A3A')
              .borderRadius(4)
              .margin({ left: 2, right: 2 })
          } else {
            Row()
              .width(8)
              .height(8)
              .backgroundColor(this.playIndex === item.symbolIndex && this.isPlaying ? '#FFB300' :
                this.playIndex > item.symbolIndex && this.isPlaying ? '#66FFB300' : '#3A3A3A')
              .borderRadius(4)
              .margin({ left: 2, right: 2 })
          }
        })
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .alignItems(VerticalAlign.Center)
      .margin({ top: 8 })
      .id('MorseSymbolHour')
      .alignRules({
        top: { anchor: 'MorseCodeMinute', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row()
        .width(40)
        .height(2)
        .backgroundColor('#666666')
        .borderRadius(1)
        .margin({ top: 8 })
        .id('MorseSymbolSplit')
        .alignRules({
          top: { anchor: 'MorseSymbolHour', align: VerticalAlign.Bottom },
          middle: { anchor: '__container__', align: HorizontalAlign.Center }
        })
      Row() {
        ForEach(this.morseSymbol.filter(m => m.groupIndex >= 2), (item: MorseSymbol) => {
          if (item.isDash) {
            Row()
              .width(24)
              .height(8)
              .backgroundColor(this.playIndex === item.symbolIndex && this.isPlaying ? '#FFB300' :
                this.playIndex > item.symbolIndex && this.isPlaying ? '#66FFB300' : '#3A3A3A')
              .borderRadius(4)
              .margin({ left: 2, right: 2 })
          } else {
            Row()
              .width(8)
              .height(8)
              .backgroundColor(this.playIndex === item.symbolIndex && this.isPlaying ? '#FFB300' :
                this.playIndex > item.symbolIndex && this.isPlaying ? '#66FFB300' : '#3A3A3A')
              .borderRadius(4)
              .margin({ left: 2, right: 2 })
          }
        })
      }
      .width('100%')
      .justifyContent(FlexAlign.Center)
      .alignItems(VerticalAlign.Center)
      .margin({ top: 8 })
      .id('MorseSymbolMinute')
      .alignRules({
        top: { anchor: 'MorseSymbolSplit', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row() {
        Column() {
          Image($r('app.media.ic_vibration'))
            .width(24)
            .height(24)
            .fillColor(this.enableVibration ? '#FFB300' : '#666666')
          Text('振动')
            .fontSize(12)
            .fontColor(this.enableVibration ? '#FFB300' : '#666666')
            .margin({ top: 4 })
        }
        .padding(12)
        .borderRadius(16)
        .backgroundColor(this.enableVibration ? '#26FFB300' : '#252525')
        .borderWidth(1)
        .borderColor(this.enableVibration ? '#80FFB300' : Color.Transparent)
        .onClick(() => {
          this.enableVibration = !this.enableVibration;
          this.viewModel.setEnableVibration(this.enableVibration);
        })
        Column() {
          Image($r('app.media.ic_sound'))
            .width(24)
            .height(24)
            .fillColor(this.enableSound ? '#FFB300' : '#666666')
          Text('声音')
            .fontSize(12)
            .fontColor(this.enableSound ? '#FFB300' : '#666666')
            .margin({ top: 4 })
        }
        .padding(12)
        .borderRadius(16)
        .backgroundColor(this.enableSound ? '#26FFB300' : '#252525')
        .borderWidth(1)
        .borderColor(this.enableSound ? '#80FFB300' : Color.Transparent)
        .onClick(() => {
          this.enableSound = !this.enableSound;
          this.viewModel.setEnableSound(this.enableSound);
        })
        if (this.hasFlashlight) {
          Column() {
            Image($r('app.media.ic_flashlight'))
              .width(24)
              .height(24)
              .fillColor(this.enableFlashlight ? '#FFB300' : '#666666')
            Text('闪光')
              .fontSize(12)
              .fontColor(this.enableFlashlight ? '#FFB300' : '#666666')
              .margin({ top: 4 })
          }
          .padding(12)
          .borderRadius(16)
          .backgroundColor(this.enableFlashlight ? '#26FFB300' : '#252525')
          .borderWidth(1)
          .borderColor(this.enableFlashlight ? '#80FFB300' : Color.Transparent)
          .onClick(() => {
            this.enableFlashlight = !this.enableFlashlight;
            this.viewModel.setEnableFlashlight(this.enableFlashlight);
          })
        }
      }
      .width('100%')
      .justifyContent(FlexAlign.SpaceEvenly)
      .margin({ top: 24 })
      .id('OutputToggles')
      .alignRules({
        top: { anchor: 'MorseSymbolMinute', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row() {
        Row() {
          Text('定时播报')
            .fontSize(18)
            .fontColor('#F5F5F5')
          Toggle({ type: ToggleType.Switch, isOn: this.isScheduled })
            .selectedColor('#FFB300')
            .onChange((isOn: boolean) => {
              this.isScheduled = isOn;
              if (isOn) {
                ToastUtils.showToast('定时播报已开启');
              } else {
                ToastUtils.showToast('定时播报已关闭');
              }
            })
        }
        .width('100%')
        .justifyContent(FlexAlign.SpaceBetween)
        .id('ScheduleTitle')
        Row() {
          Text(`${Constants.INTERVAL_15} 分钟`)
            .width('100%')
            .fontSize(14)
            .fontWeight(FontWeight.SemiBold)
            .fontColor(this.interval === Constants.INTERVAL_15 ? '#0A0A0A' : '#B0B0B0')
            .textAlign(TextAlign.Center)
            .backgroundColor(this.interval === Constants.INTERVAL_15 ? '#FFB300' : '#252525')
            .borderRadius(12)
            .padding(10)
            .onClick(() => {
              this.interval = Constants.INTERVAL_15;
              this.viewModel.setInterval(this.interval);
            })
          Text(`${Constants.INTERVAL_30} 分钟`)
            .width('100%')
            .fontSize(14)
            .fontWeight(FontWeight.SemiBold)
            .fontColor(this.interval === Constants.INTERVAL_30 ? '#0A0A0A' : '#B0B0B0')
            .textAlign(TextAlign.Center)
            .backgroundColor(this.interval === Constants.INTERVAL_30 ? '#FFB300' : '#252525')
            .borderRadius(12)
            .padding(10)
            .onClick(() => {
              this.interval = Constants.INTERVAL_30;
              this.viewModel.setInterval(this.interval);
            })
          Text(`${Constants.INTERVAL_60} 分钟`)
            .width('100%')
            .fontSize(14)
            .fontWeight(FontWeight.SemiBold)
            .fontColor(this.interval === Constants.INTERVAL_60 ? '#0A0A0A' : '#B0B0B0')
            .textAlign(TextAlign.Center)
            .backgroundColor(this.interval === Constants.INTERVAL_60 ? '#FFB300' : '#252525')
            .borderRadius(12)
            .padding(10)
            .onClick(() => {
              this.interval = Constants.INTERVAL_60;
              this.viewModel.setInterval(this.interval);
            })
        }
        .width('100%')
        .justifyContent(FlexAlign.SpaceBetween)
        .margin({ top: 12 })
        .id('ScheduleInterval')
      }
      .width('100%')
      .padding(16)
      .borderRadius(16)
      .backgroundColor('#1E1E1E')
      .margin({ top: 16 })
      .id('ScheduleSection')
      .alignRules({
        top: { anchor: 'OutputToggles', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Row() {
        Row() {
          Image($r('app.media.ic_calendar'))
            .width(24)
            .height(24)
            .fillColor(this.includeDate ? '#FFB300' : '#666666')
          Column() {
            Text('播报日期')
              .fontSize(16)
              .fontColor('#F5F5F5')
            Text('开启后将先播报日期再播报时间')
              .fontSize(14)
              .fontColor('#666666')
          }
          .alignItems(HorizontalAlign.Start)
          .margin({ left: 12 })
          Toggle({ type: ToggleType.Switch, isOn: this.includeDate })
            .selectedColor('#FFB300')
            .onChange((isOn: boolean) => {
              this.includeDate = isOn;
              this.viewModel.setIncludeDate(this.includeDate);
            })
        }
        .width('100%')
        .justifyContent(FlexAlign.SpaceBetween)
      }
      .width('100%')
      .padding(16)
      .borderRadius(16)
      .backgroundColor('#1E1E1E')
      .margin({ top: 12 })
      .id('SettingsSection')
      .alignRules({
        top: { anchor: 'ScheduleSection', align: VerticalAlign.Bottom },
        left: { anchor: '__container__', align: HorizontalAlign.Start },
        right: { anchor: '__container__', align: HorizontalAlign.End }
      })
      Button(this.isPlaying ? '■  停 止' : '▶  播 报')
        .width('100%')
        .height(56)
        .fontSize(18)
        .fontWeight(FontWeight.SemiBold)
        .fontColor(this.isPlaying ? '#FFFFFF' : '#0A0A0A')
        .backgroundColor(this.isPlaying ? '#EF5350' : '#FFB300')
        .borderRadius(28)
        .margin({ top: 12, left: 32, right: 32, bottom: 32 })
        .shadow({ radius: this.isPlaying ? 12 : 8, color: this.isPlaying ? '#4DEF5350' : '#4DFFB300', offsetY: 4 })
        .onClick(() => {
          if (this.isPlaying) {
            this.viewModel.stopMorseCode(this.signalPlayer);
            this.isPlaying = false;
            this.playIndex = -1;
          } else {
            this.viewModel.playMorseCode(this.enableVibration, this.enableSound, this.enableFlashlight, this.includeDate, this.signalPlayer);
            this.isPlaying = true;
          }
        })
        .id('PlayButton')
        .alignRules({
          top: { anchor: 'SettingsSection', align: VerticalAlign.Bottom },
          left: { anchor: '__container__', align: HorizontalAlign.Start },
          right: { anchor: '__container__', align: HorizontalAlign.End }
        })
    }
    .height('100%')
    .backgroundColor('#0A0A0A')
    .padding({ left: 24, right: 24, top: 48 })
  }
}
