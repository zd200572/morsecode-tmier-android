# Morse Code Time - HarmonyOS 版本

一个原生 HarmonyOS 应用，通过 **振动**、**声音** 和 **闪光灯** 以 Morse 电码播报当前时间。

## 功能特性

- 🔊 **多种输出方式** - 支持振动、声音（800Hz 蜂鸣音）、闪光灯三种信号输出
- ⏱️ **实时时间编码** - 将当前时间实时转换为 Morse 电码并播报
- 📅 **可选日期播报** - 可设置是否播报日期，默认仅播报时间
- ⏰ **定时播报** - 支持 15/30/60 分钟间隔自动播报
- 🎨 **现代 UI** - 深色主题，琥珀金配色，简约美观
- 📱 **可视化动画** - Morse 码可视化显示，播放时逐个高亮

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│  UI Layer (ArkUI)                                            │
│  ┌──────────────┐    ┌──────────────────┐                   │
│  │ Index        │───▶│ IndexViewModel   │                   │
│  └──────────────┘    └────────┬─────────┘                   │
└───────────────────────────────┼─────────────────────────────┘
                                │
┌───────────────────────────────┼─────────────────────────────┐
│  Core Layer                   ▼                              │
│  ┌──────────────────┐    ┌──────────────────┐               │
│  │ MorseCodeEngine  │◀───│ SignalPlayer     │               │
│  └──────────────────┘    └────────┬─────────┘               │
└───────────────────────────┼─────────────────────────────────┘
                            │
┌───────────────────────────┼─────────────────────────────────┐
│  Output Layer             ▼                                  │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │VibrationOut  │  │ SoundOutput  │  │ FlashlightOutput │   │
│  │(Vibrator API)│  │(AudioRenderer) │  │(LightSource API)│   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

- **语言**: ArkTS
- **UI 框架**: ArkUI
- **架构**: MVVM
- **最低支持**: API 12

## 权限说明

| 权限 | 用途 |
|------|------|
| `ohos.permission.KEEP_BACKGROUND_RUNNING` | 后台运行 |
| `ohos.permission.PUBLISH_AGENT_REMINDER` | 定时提醒 |
| `ohos.permission.VIBRATE` | 振动输出 |
| `ohos.permission.MANAGE_SENSOR` | 传感器管理（闪光灯） |
| `ohos.permission.WRITE_AUDIO` | 音频写入 |

## 项目结构

```
entry/src/main/ets/
├── entryability/
│   ├── EntryAbility.ets              # 入口 Ability
│   ├── MorseSchedulerServiceExtAbility.ets # 定时播报服务
│   └── NotificationUtils.ets         # 通知工具
├── pages/
│   └── Index.ets                     # 主界面
├── viewmodels/
│   └── IndexViewModel.ets            # ViewModel
├── utils/
│   ├── MorseCodeEngine.ets           # Morse 编码核心引擎
│   ├── SignalPlayer.ets              # 信号播放协调器
│   ├── VibrationOutput.ets           # 振动输出
│   ├── SoundOutput.ets               # 声音输出
│   └── LightSourceManager.ets        # 闪光灯输出
└── common/
    ├── constants/
    │   └── Constants.ets             # 常量定义
    └── utils/
        ├── Logger.ets                # 日志工具
        ├── DateUtils.ets             # 日期工具
        ├── TimeUtils.ets             # 时间工具
        └── ToastUtils.ets            # Toast 工具
```

## 安装

1. 使用 DevEco Studio 打开项目
2. 连接 HarmonyOS 设备或启动模拟器
3. 点击运行按钮安装应用

## 使用说明

### 基本操作

1. **手动播报** - 点击「播报」按钮，将以当前设置的方式播报时间
2. **停止播报** - 播报过程中点击「停止」按钮立即停止
3. **输出方式** - 通过振动、声音、闪光灯开关控制输出方式

### 设置

- **定时播报** - 开启后按设定间隔自动播报
- **播报日期** - 开启后先播报日期（年月日）再播报时间

### Morse 编码说明

时间编码示例（22:49）：
- 小时 `22` → `··−−− ··−−−`（2 的 Morse 码重复两次）
- 分钟 `49` → `····− −−−−·`（4 和 9 的 Morse 码）

信号时长：
| 信号 | 时长 |
|------|------|
| 短信号（·） | 200ms |
| 长信号（−） | 600ms |
| 符号间隔 | 200ms |
| 字符间隔 | 600ms |
| 时分分隔 | 1200ms |

## License

MIT License
