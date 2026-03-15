# Morse Code Time - HarmonyOS 项目结构

```
morsecode_harmonyos/
│
├── AppScope/                          # 应用全局配置
│   ├── app.json5                      # 应用配置文件
│   ├── hvigorfile.ts5                 # Hvigor 构建配置
│   └── resources/
│       └── base/
│           ├── element/
│           │   └── string.json        # 应用名称等字符串资源
│           └── media/
│               └── app_icon.png       # 应用图标
│
├── src/main/                          # 主模块
│   ├── ets/                           # ArkTS 源代码
│   │   ├── entryability/              # Ability 入口
│   │   │   ├── EntryAbility.ets       # 主入口 Ability
│   │   │   ├── MorseSchedulerServiceExtAbility.ets  # 定时播报服务
│   │   │   └── NotificationUtils.ets  # 通知工具类
│   │   │
│   │   ├── pages/                     # 页面
│   │   │   └── Index.ets              # 主页面
│   │   │
│   │   ├── viewmodels/                # 视图模型
│   │   │   └── IndexViewModel.ets     # 主页面 ViewModel
│   │   │
│   │   ├── utils/                     # 工具类
│   │   │   ├── MorseCodeEngine.ets    # Morse 编码核心引擎
│   │   │   ├── SignalPlayer.ets       # 信号播放协调器
│   │   │   ├── VibrationOutput.ets    # 振动输出
│   │   │   ├── SoundOutput.ets        # 声音输出
│   │   │   └── LightSourceManager.ets # 闪光灯输出
│   │   │
│   │   └── common/                    # 公共模块
│   │       ├── constants/
│   │       │   └── Constants.ets      # 常量定义
│   │       └── utils/
│   │           ├── Logger.ets         # 日志工具
│   │           ├── DateUtils.ets      # 日期工具
│   │           ├── TimeUtils.ets      # 时间工具
│   │           └── ToastUtils.ets     # Toast 工具
│   │
│   ├── module.json5                   # 模块配置文件
│   ├── build-profile.json5            # 模块构建配置
│   ├── hvigorfile.ts5                 # 模块 Hvigor 配置
│   │
│   └── resources/                     # 资源文件
│       └── base/
│           ├── element/
│           │   ├── string.json        # 字符串资源
│           │   └── color.json         # 颜色资源
│           ├── media/
│           │   ├── icon.svg           # 模块图标
│           │   ├── ic_vibration.svg   # 振动图标
│           │   ├── ic_sound.svg       # 声音图标
│           │   ├── ic_flashlight.svg  # 闪光灯图标
│           │   └── ic_calendar.svg    # 日历图标
│           └── profile/
│               └── main_pages.json    # 页面配置
│
├── build-profile.json5                # 项目构建配置
├── hvigorfile.ts5                     # 项目 Hvigor 配置
├── obfuscation-rules.txt              # 代码混淆规则
│
├── README.md                          # 项目说明
├── QUICKSTART.md                      # 快速入门指南
├── CONVERSION_SUMMARY.md              # 转换总结文档
└── PROJECT_STRUCTURE.md               # 本文件
```

## 模块说明

### AppScope
应用级别的配置和资源，包含应用图标、名称等全局信息。

### entry
主模块，包含应用的主要功能。

#### entryability
- **EntryAbility**: 应用的入口点，负责启动主页面
- **MorseSchedulerServiceExtAbility**: 后台服务，负责定时播报功能
- **NotificationUtils**: 通知工具类，用于创建和管理通知

#### pages
- **Index**: 主页面，显示时间、Morse 码和控制按钮

#### viewmodels
- **IndexViewModel**: 主页面的视图模型，管理页面状态和业务逻辑

#### utils
- **MorseCodeEngine**: Morse 编码核心引擎，负责将时间转换为 Morse 码
- **SignalPlayer**: 信号播放协调器，负责协调振动、声音、闪光灯三种输出
- **VibrationOutput**: 振动输出实现
- **SoundOutput**: 声音输出实现
- **LightSourceManager**: 闪光灯输出实现

#### common
公共模块，包含常量定义和工具类。

## 依赖关系

```
Index.ets (页面)
    ↓
IndexViewModel.ets (视图模型)
    ↓
├── MorseCodeEngine.ets (核心引擎)
├── SignalPlayer.ets (播放器)
│   ├── VibrationOutput.ets
│   ├── SoundOutput.ets
│   └── LightSourceManager.ets
└── preferences (数据存储)
```

## 资源说明

### media/
- **icon.svg**: 模块图标，显示在启动器和任务切换器中
- **ic_vibration.svg**: 振动图标，用于振动开关
- **ic_sound.svg**: 声音图标，用于声音开关
- **ic_flashlight.svg**: 闪光灯图标，用于闪光灯开关
- **ic_calendar.svg**: 日历图标，用于日期播报设置

### element/
- **string.json**: 字符串资源，包含模块描述、Ability 名称等
- **color.json**: 颜色资源，包含启动窗口背景色等

### profile/
- **main_pages.json**: 页面配置，定义应用的页面路由

## 权限说明

应用需要以下权限：

1. **ohos.permission.KEEP_BACKGROUND_RUNNING**: 后台运行权限，用于定时播报服务
2. **ohos.permission.PUBLISH_AGENT_REMINDER**: 发布提醒权限，用于定时任务
3. **ohos.permission.VIBRATE**: 振动权限，用于振动输出
4. **ohos.permission.MANAGE_SENSOR**: 传感器管理权限，用于闪光灯控制
5. **ohos.permission.WRITE_AUDIO**: 音频写入权限，用于声音输出

## 构建配置

### build-profile.json5
定义应用的构建配置，包括：
- 产品名称
- 签名配置
- 兼容的 SDK 版本
- 运行时系统

### hvigorfile.ts5
定义 Hvigor 构建工具的配置，包括：
- 内置插件
- 自定义插件

### obfuscation-rules.txt
定义代码混淆规则，指定哪些文件和类需要保持原名。

## 开发建议

### 添加新功能
1. 在 `utils/` 中添加新的工具类
2. 在 `viewmodels/` 中添加新的视图模型
3. 在 `pages/` 中添加新的页面
4. 在 `resources/` 中添加相应的资源文件

### 修改 UI
1. 编辑 `pages/Index.ets` 中的 UI 组件
2. 在 `resources/base/media/` 中添加或修改图标
3. 在 `resources/base/element/` 中修改字符串和颜色资源

### 调试
1. 使用 `Logger.ets` 中的日志工具输出调试信息
2. 使用 DevEco Studio 的调试器进行断点调试
3. 使用 DevEco Studio 的 Layout Inspector 查看 UI 布局
