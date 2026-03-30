package com.morsecode.android.morse

/**
 * Morse 电码核心引擎
 * 将时间和日期编码为 Morse 信号序列
 */
object MorseCodeEngine {

    /** 信号类型 */
    enum class SignalType { ON, OFF }

    /** 单个信号单元 */
    data class Signal(val type: SignalType, val durationMs: Long)

    /** Morse 时长参数 */
    data class MorseTimings(
        val dotDuration: Long,
        val dashDuration: Long,
        val symbolGap: Long,
        val charGap: Long,
        val groupGap: Long,
        val dateTimeGap: Long
    )

    /** 默认速度 25 WPM */
    const val DEFAULT_WPM = 25
    const val MIN_WPM = 5
    const val MAX_WPM = 40

    /** SharedPreferences key */
    const val KEY_WPM = "morse_wpm"

    /**
     * 根据 WPM 计算各时长参数
     * 标准公式: dot = 1200 / WPM (ms)
     */
    fun calculateTimings(wpm: Int): MorseTimings {
        val safeWpm = wpm.coerceIn(MIN_WPM, MAX_WPM)
        val dot = 1200L / safeWpm
        return MorseTimings(
            dotDuration = dot,
            dashDuration = dot * 3,
            symbolGap = dot,
            charGap = dot * 3,
            groupGap = dot * 7,
            dateTimeGap = dot * 10
        )
    }

    /** 数字 0-9 的 Morse 编码 */
    private val MORSE_TABLE = mapOf(
        '0' to "-----",
        '1' to ".----",
        '2' to "..---",
        '3' to "...--",
        '4' to "....-",
        '5' to ".....",
        '6' to "-....",
        '7' to "--...",
        '8' to "---..",
        '9' to "----."
    )

    /**
     * 将时间编码为 Morse 字符串
     * @return Pair<小时Morse, 分钟Morse>，每个数字之间用空格分隔
     */
    fun encodeTime(hour: Int, minute: Int): Pair<String, String> {
        val hourStr = String.format("%02d", hour)
        val minuteStr = String.format("%02d", minute)

        val hourMorse = hourStr.map { MORSE_TABLE[it] ?: "" }.joinToString(" ")
        val minuteMorse = minuteStr.map { MORSE_TABLE[it] ?: "" }.joinToString(" ")

        return Pair(hourMorse, minuteMorse)
    }

    /**
     * 将日期编码为 Morse 字符串
     * @return Triple<年Morse, 月Morse, 日Morse>
     */
    fun encodeDate(year: Int, month: Int, day: Int): Triple<String, String, String> {
        val yearStr = String.format("%04d", year)
        val monthStr = String.format("%02d", month)
        val dayStr = String.format("%02d", day)

        val yearMorse = yearStr.map { MORSE_TABLE[it] ?: "" }.joinToString(" ")
        val monthMorse = monthStr.map { MORSE_TABLE[it] ?: "" }.joinToString(" ")
        val dayMorse = dayStr.map { MORSE_TABLE[it] ?: "" }.joinToString(" ")

        return Triple(yearMorse, monthMorse, dayMorse)
    }

    /**
     * 将 Morse 字符串转换为可视化用的符号列表
     * @return 列表元素: '.' = 短信号, '-' = 长信号, ' ' = 字符间隔, '|' = 时/分分隔
     */
    fun toSymbolList(hourMorse: String, minuteMorse: String): List<Char> {
        val symbols = mutableListOf<Char>()
        for (ch in hourMorse) {
            symbols.add(ch)
        }
        symbols.add('|')
        for (ch in minuteMorse) {
            symbols.add(ch)
        }
        return symbols
    }

    /**
     * 将时/分 Morse 编码生成信号序列
     */
    fun generateSignalSequence(hour: Int, minute: Int, wpm: Int = DEFAULT_WPM): List<Signal> {
        val (hourMorse, minuteMorse) = encodeTime(hour, minute)
        val timings = calculateTimings(wpm)
        val signals = mutableListOf<Signal>()

        fun addMorseString(morse: String) {
            var i = 0
            while (i < morse.length) {
                when (morse[i]) {
                    '.' -> {
                        signals.add(Signal(SignalType.ON, timings.dotDuration))
                        if (i + 1 < morse.length && morse[i + 1] != ' ') {
                            signals.add(Signal(SignalType.OFF, timings.symbolGap))
                        }
                    }
                    '-' -> {
                        signals.add(Signal(SignalType.ON, timings.dashDuration))
                        if (i + 1 < morse.length && morse[i + 1] != ' ') {
                            signals.add(Signal(SignalType.OFF, timings.symbolGap))
                        }
                    }
                    ' ' -> {
                        signals.add(Signal(SignalType.OFF, timings.charGap))
                    }
                }
                i++
            }
        }

        addMorseString(hourMorse)
        signals.add(Signal(SignalType.OFF, timings.groupGap))
        addMorseString(minuteMorse)

        return signals
    }

    /**
     * 将日期和时间生成信号序列
     * @param includeDate 是否包含日期播报
     */
    fun generateSignalSequence(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int,
        includeDate: Boolean,
        wpm: Int = DEFAULT_WPM
    ): List<Signal> {
        val timings = calculateTimings(wpm)
        val signals = mutableListOf<Signal>()

        fun addMorseString(morse: String) {
            var i = 0
            while (i < morse.length) {
                when (morse[i]) {
                    '.' -> {
                        signals.add(Signal(SignalType.ON, timings.dotDuration))
                        if (i + 1 < morse.length && morse[i + 1] != ' ') {
                            signals.add(Signal(SignalType.OFF, timings.symbolGap))
                        }
                    }
                    '-' -> {
                        signals.add(Signal(SignalType.ON, timings.dashDuration))
                        if (i + 1 < morse.length && morse[i + 1] != ' ') {
                            signals.add(Signal(SignalType.OFF, timings.symbolGap))
                        }
                    }
                    ' ' -> {
                        signals.add(Signal(SignalType.OFF, timings.charGap))
                    }
                }
                i++
            }
        }

        // 先播报日期（如果启用）
        if (includeDate) {
            val (yearMorse, monthMorse, dayMorse) = encodeDate(year, month, day)
            addMorseString(yearMorse)
            signals.add(Signal(SignalType.OFF, timings.groupGap))
            addMorseString(monthMorse)
            signals.add(Signal(SignalType.OFF, timings.groupGap))
            addMorseString(dayMorse)
            signals.add(Signal(SignalType.OFF, timings.dateTimeGap))
        }

        // 播报时间
        val (hourMorse, minuteMorse) = encodeTime(hour, minute)
        addMorseString(hourMorse)
        signals.add(Signal(SignalType.OFF, timings.groupGap))
        addMorseString(minuteMorse)

        return signals
    }

    /**
     * 获取 Morse 字符串中用于 UI 显示的符号列表（不含空格）
     * 每个元素代表一个 dot 或 dash
     */
    data class MorseSymbol(val isDash: Boolean, val groupIndex: Int, val symbolIndex: Int)

    fun getDisplaySymbols(hour: Int, minute: Int): List<MorseSymbol> {
        val (hourMorse, minuteMorse) = encodeTime(hour, minute)
        val symbols = mutableListOf<MorseSymbol>()
        var globalIdx = 0

        fun parseMorse(morse: String, groupStart: Int) {
            var charIdx = groupStart
            for (ch in morse) {
                when (ch) {
                    '.' -> symbols.add(MorseSymbol(false, charIdx, globalIdx++))
                    '-' -> symbols.add(MorseSymbol(true, charIdx, globalIdx++))
                    ' ' -> charIdx++
                }
            }
        }

        parseMorse(hourMorse, 0)
        parseMorse(minuteMorse, 2)

        return symbols
    }

    /**
     * 获取日期和时间显示用的符号列表
     */
    fun getDisplaySymbols(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int,
        includeDate: Boolean
    ): List<MorseSymbol> {
        val symbols = mutableListOf<MorseSymbol>()
        var globalIdx = 0

        fun parseMorse(morse: String, groupStart: Int) {
            var charIdx = groupStart
            for (ch in morse) {
                when (ch) {
                    '.' -> symbols.add(MorseSymbol(false, charIdx, globalIdx++))
                    '-' -> symbols.add(MorseSymbol(true, charIdx, globalIdx++))
                    ' ' -> charIdx++
                }
            }
        }

        var currentGroup = 0

        if (includeDate) {
            val (yearMorse, monthMorse, dayMorse) = encodeDate(year, month, day)
            parseMorse(yearMorse, currentGroup)
            currentGroup += 4 // 年有4位数字
            parseMorse(monthMorse, currentGroup)
            currentGroup += 2
            parseMorse(dayMorse, currentGroup)
            currentGroup += 2
        }

        val (hourMorse, minuteMorse) = encodeTime(hour, minute)
        parseMorse(hourMorse, currentGroup)
        parseMorse(minuteMorse, currentGroup + 2)

        return symbols
    }
}
