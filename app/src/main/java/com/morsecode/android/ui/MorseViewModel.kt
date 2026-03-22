package com.morsecode.android.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.morsecode.android.morse.MorseCodeEngine
import com.morsecode.android.service.MorseSchedulerService
import com.morsecode.android.signal.SignalPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/** 播报模式 */
enum class BroadcastMode {
    /** 依次播报所有时区 */
    ALL_SEQUENTIAL,
    /** 只播报选中的时区 */
    SINGLE,
    /** 播报本地时间（原有行为） */
    LOCAL
}

/** 单个时区的显示数据 */
data class TimezoneDisplay(
    val name: String,
    val zoneId: String,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val hourMorse: String = "",
    val minuteMorse: String = ""
)

data class MorseUiState(
    val year: Int = 0,
    val month: Int = 0,
    val day: Int = 0,
    val hour: Int = 0,
    val minute: Int = 0,
    val second: Int = 0,
    val hourMorse: String = "",
    val minuteMorse: String = "",
    val displaySymbols: List<MorseCodeEngine.MorseSymbol> = emptyList(),
    val isPlaying: Boolean = false,
    val currentPlayingIndex: Int = -1,
    val totalSignals: Int = 0,
    val enableVibration: Boolean = true,
    val enableSound: Boolean = true,
    val enableFlashlight: Boolean = false,
    val includeDate: Boolean = false,
    val isScheduled: Boolean = false,
    val scheduleInterval: Int = 60,
    // 多时区
    val timezoneDisplays: List<TimezoneDisplay> = emptyList(),
    val broadcastMode: BroadcastMode = BroadcastMode.LOCAL,
    val selectedTimezoneIndex: Int = 0
)

class MorseViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val KEY_BROADCAST_MODE = "broadcast_mode"
        const val KEY_SELECTED_TZ = "selected_timezone"

        val TIMEZONE_LIST = listOf(
            TimezoneDisplay("UTC", "UTC"),
            TimezoneDisplay("莫斯科", "Europe/Moscow"),
            TimezoneDisplay("北京", "Asia/Shanghai"),
            TimezoneDisplay("纽约", "America/New_York")
        )
    }

    private val _uiState = MutableStateFlow(MorseUiState())
    val uiState: StateFlow<MorseUiState> = _uiState.asStateFlow()

    val signalPlayer = SignalPlayer(application)
    private val prefs = application.getSharedPreferences(
        MorseSchedulerService.PREFS_NAME, Context.MODE_PRIVATE
    )

    init {
        // 读取持久化的设置
        val modeOrd = prefs.getInt(KEY_BROADCAST_MODE, BroadcastMode.LOCAL.ordinal)
        val broadcastMode = BroadcastMode.entries.getOrElse(modeOrd) { BroadcastMode.LOCAL }

        _uiState.value = _uiState.value.copy(
            enableVibration = prefs.getBoolean(MorseSchedulerService.KEY_VIBRATION, true),
            enableSound = prefs.getBoolean(MorseSchedulerService.KEY_SOUND, true),
            enableFlashlight = prefs.getBoolean(MorseSchedulerService.KEY_FLASHLIGHT, false),
            includeDate = prefs.getBoolean(MorseSchedulerService.KEY_INCLUDE_DATE, false),
            isScheduled = prefs.getBoolean("is_scheduled", false),
            scheduleInterval = prefs.getInt(MorseSchedulerService.KEY_INTERVAL, 60),
            broadcastMode = broadcastMode,
            selectedTimezoneIndex = prefs.getInt(KEY_SELECTED_TZ, 0)
        )

        // 每秒更新时间
        viewModelScope.launch {
            while (isActive) {
                updateTime()
                delay(1000)
            }
        }

        // 设置播放回调
        signalPlayer.onProgress = { current, total ->
            _uiState.value = _uiState.value.copy(
                currentPlayingIndex = current,
                totalSignals = total
            )
        }
        signalPlayer.onComplete = {
            _uiState.value = _uiState.value.copy(
                isPlaying = false,
                currentPlayingIndex = -1
            )
        }
    }

    private fun updateTime() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val second = cal.get(Calendar.SECOND)
        val (hourMorse, minuteMorse) = MorseCodeEngine.encodeTime(hour, minute)
        val symbols = MorseCodeEngine.getDisplaySymbols(hour, minute)

        // 计算各时区时间
        val tzDisplays = TIMEZONE_LIST.map { tz ->
            val tzCal = Calendar.getInstance(TimeZone.getTimeZone(tz.zoneId))
            val tzH = tzCal.get(Calendar.HOUR_OF_DAY)
            val tzM = tzCal.get(Calendar.MINUTE)
            val tzS = tzCal.get(Calendar.SECOND)
            val (hm, mm) = MorseCodeEngine.encodeTime(tzH, tzM)
            tz.copy(hour = tzH, minute = tzM, second = tzS, hourMorse = hm, minuteMorse = mm)
        }

        _uiState.value = _uiState.value.copy(
            year = year,
            month = month,
            day = day,
            hour = hour,
            minute = minute,
            second = second,
            hourMorse = hourMorse,
            minuteMorse = minuteMorse,
            displaySymbols = symbols,
            timezoneDisplays = tzDisplays
        )
    }

    fun togglePlay() {
        if (signalPlayer.isPlaying) {
            signalPlayer.stop()
            _uiState.value = _uiState.value.copy(
                isPlaying = false,
                currentPlayingIndex = -1
            )
        } else {
            val state = _uiState.value
            signalPlayer.enableVibration = state.enableVibration
            signalPlayer.enableSound = state.enableSound
            signalPlayer.enableFlashlight = state.enableFlashlight

            when (state.broadcastMode) {
                BroadcastMode.LOCAL -> {
                    signalPlayer.play(
                        state.year, state.month, state.day,
                        state.hour, state.minute,
                        state.includeDate,
                        viewModelScope
                    )
                }
                BroadcastMode.SINGLE -> {
                    val tz = state.timezoneDisplays.getOrNull(state.selectedTimezoneIndex)
                    if (tz != null) {
                        signalPlayer.play(tz.hour, tz.minute, viewModelScope)
                    }
                }
                BroadcastMode.ALL_SEQUENTIAL -> {
                    val timeList = state.timezoneDisplays.map { it.hour to it.minute }
                    signalPlayer.playMultipleTimezones(timeList, viewModelScope)
                }
            }
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun setVibration(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableVibration = enabled)
        prefs.edit().putBoolean(MorseSchedulerService.KEY_VIBRATION, enabled).apply()
    }

    fun setSound(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableSound = enabled)
        prefs.edit().putBoolean(MorseSchedulerService.KEY_SOUND, enabled).apply()
    }

    fun setFlashlight(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(enableFlashlight = enabled)
        prefs.edit().putBoolean(MorseSchedulerService.KEY_FLASHLIGHT, enabled).apply()
    }

    fun setIncludeDate(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(includeDate = enabled)
        prefs.edit().putBoolean(MorseSchedulerService.KEY_INCLUDE_DATE, enabled).apply()
    }

    fun setBroadcastMode(mode: BroadcastMode) {
        _uiState.value = _uiState.value.copy(broadcastMode = mode)
        prefs.edit().putInt(KEY_BROADCAST_MODE, mode.ordinal).apply()
    }

    fun setSelectedTimezone(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTimezoneIndex = index)
        prefs.edit().putInt(KEY_SELECTED_TZ, index).apply()
    }

    fun setScheduleInterval(interval: Int) {
        _uiState.value = _uiState.value.copy(scheduleInterval = interval)
        prefs.edit().putInt(MorseSchedulerService.KEY_INTERVAL, interval).apply()
        if (_uiState.value.isScheduled) {
            MorseSchedulerService.start(getApplication(), interval)
        }
    }

    fun toggleSchedule() {
        val isScheduled = !_uiState.value.isScheduled
        _uiState.value = _uiState.value.copy(isScheduled = isScheduled)
        if (isScheduled) {
            MorseSchedulerService.start(getApplication(), _uiState.value.scheduleInterval)
        } else {
            MorseSchedulerService.stop(getApplication())
        }
    }

    override fun onCleared() {
        signalPlayer.release()
        super.onCleared()
    }
}
