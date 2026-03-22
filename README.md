# Morse Code Time ⏰

一个原生 Android 应用，通过 **振动**、**声音** 和 **闪光灯** 以 Morse 电码播报当前时间。

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![MinSDK](https://img.shields.io/badge/MinSDK-26+-blue.svg)](https://developer.android.com/about/versions/o)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#license)

## 功能特性

- 🔊 **多种输出方式** - 支持振动、声音（800Hz 蜂鸣音）、闪光灯三种信号输出
- ⏱️ **实时时间编码** - 将当前时间实时转换为 Morse 电码并播报
- 📅 **可选日期播报** - 可设置是否播报日期，默认仅播报时间
- ⏰ **定时播报** - 支持 15/30/60 分钟间隔自动播报
- 🎨 **现代 UI** - Material 3 深色主题，琥珀金配色，简约美观
- 📱 **可视化动画** - Morse 码可视化显示，播放时逐个高亮

## 截图

```
┌─────────────────────────────┐
│         22 : 49             │  ← 大号时间显示
│                             │
│    ··−−−  ··−−−             │  ← 小时 Morse 码
│    ·−···  −−−−·             │  ← 分钟 Morse 码
│                             │
│  ● ● ━ ━ ━  ● ● ━ ━ ━       │  ← 可视化显示
│  ● ━ ● ● ●  ━ ━ ━ ━ ●       │     播放时逐个高亮
│                             │
│  [振动 ✓]  [声音 ✓]  [闪光]  │  ← 输出开关
│                             │
│  定时播报  [15] 30  60 分钟   │  ← 间隔选择
│                             │
│  播报设置                    │
│  [📅] 播报日期 ──────── [ ]  │  ← 日期播报开关
│                             │
│        ╭──────────╮          │
│        │  ▶ 播 报  │          │  ← 主按钮
│        ╰──────────╯          │
└─────────────────────────────┘
```

## 安装

### 从源码构建

1. 克隆仓库
```bash
git clone https://github.com/yourusername/morsecode-android.git
cd morsecode-android
```

2. 生成 Gradle Wrapper（如需要）
```bash
gradle wrapper --gradle-version=8.9
```

3. 构建 APK
```bash
./gradlew assembleDebug
```

4. 安装到设备
```bash
./gradlew installDebug
```

### 系统要求

- Android 8.0 (API 26) 或更高版本
- 部分功能需要硬件支持：
  - 振动：需要设备振动器
  - 闪光灯：需要设备闪光灯

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

## 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)                                 │
│  ┌──────────────┐    ┌──────────────────┐                   │
│  │ MainScreen   │───▶│ MorseViewModel   │                   │
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
│  │(Vibrator API)│  │(AudioTrack)  │  │(Camera2 torch)   │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### 技术栈

- **语言**: Kotlin
- **UI 框架**: Jetpack Compose + Material 3
- **架构**: MVVM (ViewModel + StateFlow)
- **异步**: Kotlin Coroutines
- **最低支持**: Android 8.0 (API 26)

## 权限说明

| 权限 | 用途 |
|------|------|
| `VIBRATE` | 振动输出 |
| `CAMERA` / `FLASHLIGHT` | 闪光灯控制 |
| `FOREGROUND_SERVICE` | 定时播报后台服务 |
| `SCHEDULE_EXACT_ALARM` | 精确闹钟调度 |
| `POST_NOTIFICATIONS` | 通知显示 (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | 开机后恢复定时任务 |

## 项目结构

```
app/src/main/java/com/morsecode/android/
├── MainActivity.kt              # 入口 Activity
├── morse/
│   └── MorseCodeEngine.kt       # Morse 编码核心引擎
├── signal/
│   ├── SignalPlayer.kt          # 信号播放协调器
│   ├── VibrationOutput.kt       # 振动输出
│   ├── SoundOutput.kt           # 声音输出
│   └── FlashlightOutput.kt      # 闪光灯输出
├── service/
│   ├── MorseSchedulerService.kt # 定时播报服务
│   ├── MorseAlarmReceiver.kt    # 闹钟接收器
│   └── BootReceiver.kt          # 开机启动接收器
└── ui/
    ├── MainScreen.kt            # 主界面
    ├── MorseViewModel.kt        # ViewModel
    └── theme/                   # 主题配置
```

## TODO

### 多时区时间显示
- [ ] 在界面上显示多个时区时间
  - UTC（协调世界时）
  - 莫斯科（UTC+3）
  - 北京（UTC+8）
  - 纽约（UTC-5/-4 夏令时）
- [ ] 时区时间一目了然，方便查看各处时间

### 摩尔斯码播报选项
- [ ] 添加播报模式选项
  - **依次播报**：按顺序播报各时区时间
  - **单项播报**：只播报选中的某个时区
  - **当前时间播报**：播报本地时间
- [ ] 在设置界面添加播报模式选择

---

## 贡献

欢迎提交 Issue 和 Pull Request！

## License

[MIT License](LICENSE)

Copyright (c) 2025 Morse Code Time Contributors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
