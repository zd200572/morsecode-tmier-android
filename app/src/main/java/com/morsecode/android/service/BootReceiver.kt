package com.morsecode.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 开机广播接收器 - 恢复定时播报任务
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (MorseSchedulerService.isScheduled(context)) {
                val prefs = context.getSharedPreferences(
                    MorseSchedulerService.PREFS_NAME, Context.MODE_PRIVATE
                )
                val interval = prefs.getInt(MorseSchedulerService.KEY_INTERVAL, 60)
                MorseSchedulerService.start(context, interval)
            }
        }
    }
}
