package com.morsecode.android.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 闹钟广播接收器
 */
class MorseAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 通过启动 Service 来执行播报
        val serviceIntent = Intent(context, MorseSchedulerService::class.java)
        context.startForegroundService(serviceIntent)
    }
}
