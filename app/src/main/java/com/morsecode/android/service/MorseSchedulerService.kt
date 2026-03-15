package com.morsecode.android.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import androidx.core.app.NotificationCompat
import com.morsecode.android.MainActivity
import com.morsecode.android.signal.SignalPlayer
import kotlinx.coroutines.*
import java.util.Calendar

/**
 * 前台服务 - 定时 Morse 播报
 */
class MorseSchedulerService : Service() {

    companion object {
        const val CHANNEL_ID = "morse_scheduler_channel"
        const val NOTIFICATION_ID = 1001
        const val PREFS_NAME = "morse_prefs"
        const val KEY_INTERVAL = "schedule_interval"
        const val KEY_VIBRATION = "enable_vibration"
        const val KEY_SOUND = "enable_sound"
        const val KEY_FLASHLIGHT = "enable_flashlight"
        const val KEY_INCLUDE_DATE = "include_date"

        const val ACTION_START = "com.morsecode.android.START_SCHEDULER"
        const val ACTION_STOP = "com.morsecode.android.STOP_SCHEDULER"
        const val EXTRA_INTERVAL = "interval_minutes"

        fun start(context: Context, intervalMinutes: Int) {
            val intent = Intent(context, MorseSchedulerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_INTERVAL, intervalMinutes)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MorseSchedulerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun isScheduled(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean("is_scheduled", false)
        }
    }

    private var signalPlayer: SignalPlayer? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var prefs: SharedPreferences
    private lateinit var alarmManager: AlarmManager

    override fun onCreate() {
        super.onCreate()
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        createNotificationChannel()
        signalPlayer = SignalPlayer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val interval = intent.getIntExtra(EXTRA_INTERVAL, 60)
                prefs.edit()
                    .putInt(KEY_INTERVAL, interval)
                    .putBoolean("is_scheduled", true)
                    .apply()
                startForeground(NOTIFICATION_ID, buildNotification(interval))
                scheduleNextAlarm(interval)
            }
            ACTION_STOP -> {
                cancelAlarm()
                prefs.edit().putBoolean("is_scheduled", false).apply()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                // 闹钟触发 — 播放 Morse 并设置下次
                playMorseNow()
                val interval = prefs.getInt(KEY_INTERVAL, 60)
                scheduleNextAlarm(interval)
            }
        }
        return START_STICKY
    }

    private fun playMorseNow() {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val includeDate = prefs.getBoolean(KEY_INCLUDE_DATE, false)

        signalPlayer?.let { player ->
            player.enableVibration = prefs.getBoolean(KEY_VIBRATION, true)
            player.enableSound = prefs.getBoolean(KEY_SOUND, true)
            player.enableFlashlight = prefs.getBoolean(KEY_FLASHLIGHT, false)
            player.play(year, month, day, hour, minute, includeDate, serviceScope)
        }
    }

    private fun scheduleNextAlarm(intervalMinutes: Int) {
        val cal = Calendar.getInstance()
        val currentMinute = cal.get(Calendar.MINUTE)

        // 计算下一个整点/半点/刻钟时刻
        // 例如：interval=15 时，播报时刻为 :00, :15, :30, :45
        // interval=30 时，播报时刻为 :00, :30
        // interval=60 时，播报时刻为 :00（整点）
        val nextMinute = ((currentMinute / intervalMinutes) + 1) * intervalMinutes

        // 如果超过60分钟，进位到下一小时
        if (nextMinute >= 60) {
            cal.add(Calendar.HOUR_OF_DAY, 1)
            cal.set(Calendar.MINUTE, 0)
        } else {
            cal.set(Calendar.MINUTE, nextMinute)
        }
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val pendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MorseSchedulerService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm() {
        val pendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, MorseSchedulerService::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Morse 定时播报",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Morse 电码定时播报服务通知"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(intervalMinutes: Int): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Morse 报时运行中")
            .setContentText("每 ${intervalMinutes} 分钟自动播报")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        signalPlayer?.release()
        serviceScope.cancel()
        super.onDestroy()
    }
}
