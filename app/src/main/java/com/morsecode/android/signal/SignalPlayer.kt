package com.morsecode.android.signal

import android.content.Context
import com.morsecode.android.morse.MorseCodeEngine
import com.morsecode.android.morse.MorseCodeEngine.SignalType
import kotlinx.coroutines.*

/**
 * 统一信号播放控制器
 * 协调振动、声音和闪光灯三种输出
 */
class SignalPlayer(context: Context) {

    val vibrationOutput = VibrationOutput(context)
    val soundOutput = SoundOutput()
    val flashlightOutput = FlashlightOutput(context)

    var enableVibration = true
    var enableSound = true
    var enableFlashlight = false

    var isPlaying = false
        private set

    private var playJob: Job? = null

    /** 播放进度回调：当前信号索引，总信号数 */
    var onProgress: ((currentIndex: Int, total: Int) -> Unit)? = null
    var onComplete: (() -> Unit)? = null

    /**
     * 播放指定时间的 Morse 信号序列（仅时间）
     */
    fun play(hour: Int, minute: Int, scope: CoroutineScope) {
        play(0, 0, 0, hour, minute, false, scope)
    }

    /**
     * 播放日期和时间的 Morse 信号序列
     * @param includeDate 是否包含日期播报
     */
    fun play(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int,
        includeDate: Boolean,
        scope: CoroutineScope
    ) {
        if (isPlaying) return
        val signals = MorseCodeEngine.generateSignalSequence(
            year, month, day, hour, minute, includeDate
        )
        isPlaying = true

        playJob = scope.launch(Dispatchers.Main) {
            try {
                var signalIdx = 0
                for ((index, signal) in signals.withIndex()) {
                    if (!isActive) break

                    if (signal.type == SignalType.ON) {
                        onProgress?.invoke(signalIdx, signals.count { it.type == SignalType.ON })
                        signalIdx++
                        if (enableVibration) vibrationOutput.vibrate(signal.durationMs)
                        if (enableSound) soundOutput.play(signal.durationMs)
                        if (enableFlashlight) flashlightOutput.turnOn()
                    } else {
                        if (enableFlashlight) flashlightOutput.turnOff()
                    }

                    delay(signal.durationMs)
                }
            } finally {
                stopOutputs()
                isPlaying = false
                onComplete?.invoke()
            }
        }
    }

    /**
     * 依次播报多个时区的 Morse 信号
     * 每个时区之间加较长间隔
     */
    fun playMultipleTimezones(
        timeList: List<Pair<Int, Int>>,
        scope: CoroutineScope
    ) {
        if (isPlaying) return
        // 把每个时区的信号序列拼在一起，时区间加长间隔
        val allSignals = mutableListOf<MorseCodeEngine.Signal>()
        timeList.forEachIndexed { idx, (hour, minute) ->
            allSignals.addAll(MorseCodeEngine.generateSignalSequence(hour, minute))
            if (idx < timeList.size - 1) {
                allSignals.add(MorseCodeEngine.Signal(SignalType.OFF, MorseCodeEngine.DATE_TIME_GAP))
            }
        }

        isPlaying = true
        playJob = scope.launch(Dispatchers.Main) {
            try {
                var signalIdx = 0
                val totalOn = allSignals.count { it.type == SignalType.ON }
                for (signal in allSignals) {
                    if (!isActive) break

                    if (signal.type == SignalType.ON) {
                        onProgress?.invoke(signalIdx, totalOn)
                        signalIdx++
                        if (enableVibration) vibrationOutput.vibrate(signal.durationMs)
                        if (enableSound) soundOutput.play(signal.durationMs)
                        if (enableFlashlight) flashlightOutput.turnOn()
                    } else {
                        if (enableFlashlight) flashlightOutput.turnOff()
                    }

                    delay(signal.durationMs)
                }
            } finally {
                stopOutputs()
                isPlaying = false
                onComplete?.invoke()
            }
        }
    }

    fun stop() {
        playJob?.cancel()
        playJob = null
        stopOutputs()
        isPlaying = false
    }

    private fun stopOutputs() {
        vibrationOutput.stop()
        soundOutput.stop()
        flashlightOutput.stop()
    }

    fun release() {
        stop()
        soundOutput.stop()
    }
}
