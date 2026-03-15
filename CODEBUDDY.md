# CODEBUDDY.md

This file provides guidance to CodeBuddy Code when working with code in this repository.

## Project Overview

Morse Code Time is a native Android app that announces the current time using Morse code through vibration, sound, and flashlight. Built with Kotlin + Jetpack Compose + Material 3, targeting API 26+ (Android 8.0).

## Build Commands

This project uses Gradle with Kotlin DSL. The project does not include gradle wrapper - use system gradle or generate wrapper first:

```bash
# Generate gradle wrapper (if not present)
gradle wrapper

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/com/morsecode/android/
├── MainActivity.kt              # Entry point, sets up Compose UI
├── morse/
│   └── MorseCodeEngine.kt       # Core: time → Morse encoding, signal sequence generation
├── signal/
│   ├── SignalPlayer.kt          # Coordinator for all output types
│   ├── VibrationOutput.kt       # Vibrator API wrapper
│   ├── SoundOutput.kt           # AudioTrack 800Hz sine wave generator
│   └── FlashlightOutput.kt      # Camera2 torch mode controller
├── service/
│   ├── MorseSchedulerService.kt # Foreground service for scheduled announcements
│   ├── MorseAlarmReceiver.kt    # BroadcastReceiver for alarm triggers
│   └── BootReceiver.kt          # Restores schedule after device reboot
└── ui/
    ├── MainScreen.kt            # Main Compose UI
    ├── MorseViewModel.kt        # ViewModel with StateFlow
    └── theme/                   # Material 3 dark theme (amber accent)
```

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  UI Layer (Compose)                                         │
│  ┌──────────────┐    ┌──────────────────┐                   │
│  │ MainScreen   │───▶│ MorseViewModel   │                   │
│  └──────────────┘    └────────┬─────────┘                   │
│                               │ StateFlow<MorseUiState>      │
└───────────────────────────────┼─────────────────────────────┘
                                │
┌───────────────────────────────┼─────────────────────────────┐
│  Core Layer                   ▼                              │
│  ┌──────────────────┐    ┌──────────────────┐               │
│  │ MorseCodeEngine  │◀───│ SignalPlayer     │               │
│  │ (object singleton)│    │ (coroutine-based │               │
│  └──────────────────┘    │  sequencer)      │               │
│                          └────────┬─────────┘               │
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

## Key Components

### MorseCodeEngine (morse/MorseCodeEngine.kt)
- `encodeTime(hour, minute)` → Pair of hour/minute Morse strings
- `generateSignalSequence(hour, minute)` → List of ON/OFF signals with durations
- Time parameters: dot=200ms, dash=600ms, symbol gap=200ms, char gap=600ms, group gap=1200ms

### SignalPlayer (signal/SignalPlayer.kt)
- Coordinates parallel output across enabled channels
- Uses coroutines for signal sequencing
- Provides progress callbacks for UI animation sync

### MorseSchedulerService (service/MorseSchedulerService.kt)
- Foreground service with persistent notification
- Uses AlarmManager.setExactAndAllowWhileIdle() for precise timing
- Supports 15/30/60 minute intervals
- Persists settings via SharedPreferences

### MorseViewModel (ui/MorseViewModel.kt)
- Single `MorseUiState` data class for all UI state
- Updates time every second
- Manages play/stop and settings persistence

## Theme

Material 3 dark theme with amber accent (#FFB300). Key colors defined in `ui/theme/Color.kt`:
- `Amber500` - primary accent
- `DarkBackground` (#121212) - background
- `DarkCard`, `DarkSurface`, `DarkSurfaceVariant` - surface variants

## Permissions Required

- `VIBRATE` - vibration output
- `CAMERA` + `FLASHLIGHT` - flashlight control
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` - scheduled announcements
- `SCHEDULE_EXACT_ALARM` - precise timing
- `POST_NOTIFICATIONS` - Android 13+ notification permission
- `RECEIVE_BOOT_COMPLETED` - restore schedule after reboot

## Testing Notes

Hardware features (vibration, flashlight) require physical device testing. Emulator can test UI and sound output.
