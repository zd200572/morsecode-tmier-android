package com.morsecode.android.signal

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.sin

/**
 * 声音输出实现 - 生成 800Hz 正弦波蜂鸣音
 */
class SoundOutput {

    private val sampleRate = 44100
    private val frequency = 800.0
    private var audioTrack: AudioTrack? = null

    fun play(durationMs: Long) {
        stop()
        val numSamples = (sampleRate * durationMs / 1000).toInt()
        val samples = ShortArray(numSamples)

        // 生成正弦波
        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * frequency * i / sampleRate
            samples[i] = (sin(angle) * Short.MAX_VALUE * 0.8).toInt().toShort()
        }

        // 添加淡入淡出（避免爆音）
        val fadeLen = minOf(numSamples / 10, 500)
        for (i in 0 until fadeLen) {
            val factor = i.toDouble() / fadeLen
            samples[i] = (samples[i] * factor).toInt().toShort()
            samples[numSamples - 1 - i] = (samples[numSamples - 1 - i] * factor).toInt().toShort()
        }

        val bufferSize = samples.size * 2 // Short = 2 bytes
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(sampleRate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack?.write(samples, 0, samples.size)
        audioTrack?.play()
    }

    fun stop() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) { }
        audioTrack = null
    }
}
