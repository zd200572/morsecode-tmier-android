package com.morsecode.android.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.morsecode.android.MainActivity
import com.morsecode.android.R
import com.morsecode.android.morse.MorseCodeEngine
import com.morsecode.android.service.MorseSchedulerService
import com.morsecode.android.signal.SignalPlayer
import kotlinx.coroutines.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Morse 报时桌面小工具
 * 显示多时区时间和对应的 Morse 码，支持一键播报
 */
class MorseWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_PLAY_MORSE = "com.morsecode.android.widget.PLAY_MORSE"

        private var signalPlayer: SignalPlayer? = null
        private val widgetScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        // 时区定义
        private val ZONE_UTC = ZoneId.of("UTC")
        private val ZONE_MOSCOW = ZoneId.of("Europe/Moscow")
        private val ZONE_BEIJING = ZoneId.of("Asia/Shanghai")
        private val ZONE_NEW_YORK = ZoneId.of("America/New_York")

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        /**
         * 手动触发所有 widget 更新（可被外部调用）
         */
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, MorseWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                val intent = Intent(context, MorseWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_PLAY_MORSE) {
            playMorse(context)
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        signalPlayer?.release()
        signalPlayer = null
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val now = Instant.now()

        // 计算各时区时间
        val localTime = ZonedDateTime.ofInstant(now, ZoneId.systemDefault())
        val utcTime = ZonedDateTime.ofInstant(now, ZONE_UTC)
        val moscowTime = ZonedDateTime.ofInstant(now, ZONE_MOSCOW)
        val beijingTime = ZonedDateTime.ofInstant(now, ZONE_BEIJING)
        val nyTime = ZonedDateTime.ofInstant(now, ZONE_NEW_YORK)

        // 格式化时间字符串
        val localTimeStr = "本地 " + localTime.format(timeFormatter)
        val utcTimeStr = utcTime.format(timeFormatter)
        val moscowTimeStr = moscowTime.format(timeFormatter)
        val beijingTimeStr = beijingTime.format(timeFormatter)
        val nyTimeStr = nyTime.format(timeFormatter)

        // 生成本地时间的 Morse 码
        val hour = localTime.hour
        val minute = localTime.minute
        val (hourMorse, minuteMorse) = MorseCodeEngine.encodeTime(hour, minute)

        // Morse 码显示：合并小时和分钟
        val morseDisplay = (hourMorse + " " + minuteMorse)
            .replace('.', '·')
            .replace('-', '−')

        val views = RemoteViews(context.packageName, R.layout.morse_widget_layout)

        // 设置时间文本
        views.setTextViewText(R.id.widget_local_time, localTimeStr)
        views.setTextViewText(R.id.widget_utc_time, utcTimeStr)
        views.setTextViewText(R.id.widget_moscow_time, moscowTimeStr)
        views.setTextViewText(R.id.widget_beijing_time, beijingTimeStr)
        views.setTextViewText(R.id.widget_ny_time, nyTimeStr)

        // 设置 Morse 码
        views.setTextViewText(R.id.widget_morse, morseDisplay)

        // 点击整体区域 → 打开 APP
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPending = PendingIntent.getActivity(
            context, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPending)

        // 点击播放按钮 → 播报 Morse
        val playIntent = Intent(context, MorseWidgetProvider::class.java).apply {
            action = ACTION_PLAY_MORSE
        }
        val playPending = PendingIntent.getBroadcast(
            context, 1, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_play_button, playPending)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun playMorse(context: Context) {
        if (signalPlayer == null) {
            signalPlayer = SignalPlayer(context.applicationContext)
        }

        val player = signalPlayer ?: return
        if (player.isPlaying) {
            player.stop()
            return
        }

        val prefs = context.getSharedPreferences(
            MorseSchedulerService.PREFS_NAME, Context.MODE_PRIVATE
        )
        player.enableVibration = prefs.getBoolean(MorseSchedulerService.KEY_VIBRATION, true)
        player.enableSound = prefs.getBoolean(MorseSchedulerService.KEY_SOUND, true)
        player.enableFlashlight = prefs.getBoolean(MorseSchedulerService.KEY_FLASHLIGHT, false)

        val wpm = prefs.getInt(MorseCodeEngine.KEY_WPM, MorseCodeEngine.DEFAULT_WPM)

        val now = ZonedDateTime.now()
        val hour = now.hour
        val minute = now.minute

        player.play(hour, minute, widgetScope, wpm)
    }
}
