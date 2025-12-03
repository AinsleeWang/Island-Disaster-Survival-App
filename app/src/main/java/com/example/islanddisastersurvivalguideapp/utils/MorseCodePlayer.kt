package com.example.islanddisastersurvivalguideapp.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.*
import kotlin.math.PI
import kotlin.math.sin

class MorseCodePlayer(private val context: Context) {
    private var audioTrack: AudioTrack? = null
    private val sampleRate = 44100
    private val dotDuration = 100 // 點的持續時間（毫秒）
    private val frequency = 800.0 // 聲音頻率（Hz）
    private var playJob: Job? = null
    private var isPlaying = false
    private val playerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val morseMap = mapOf(
        '0' to "-----", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....",
        '6' to "-....", '7' to "--...", '8' to "---..",
        '9' to "----.", '.' to ".-.-.-", 'N' to "-.",
        'E' to "."
    )

    init {
        initAudioTrack()
    }

    private fun initAudioTrack() {
        val bufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
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
            .build()
    }

    fun convertCoordinateToMorse(latitude: Double, longitude: Double): String {
        val latStr = String.format("%.7f", latitude) + "N"
        val lonStr = String.format("%.7f", longitude) + "E"
        return coordinateToMorse(latStr) + " " + coordinateToMorse(lonStr)
    }

    private fun coordinateToMorse(coordinate: String): String {
        return coordinate.map { char ->
            morseMap[char] ?: ""
        }.joinToString(" ")
    }

    fun playMorseCode(morseCode: String) {
        if (isPlaying) return
        isPlaying = true

        val samples = generateMorseSound(morseCode)
        audioTrack?.play()

        playJob = playerScope.launch {
            try {
                while (isPlaying && isActive) {
                    audioTrack?.write(samples, 0, samples.size)
                    // 添加短暫延遲避免 CPU 過度使用
                    delay(100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopPlaying() {
        isPlaying = false
        playJob?.cancel()
        audioTrack?.pause()
        audioTrack?.flush()
    }

    private fun generateMorseSound(morseCode: String): ShortArray {
        val dotSamples = (sampleRate * dotDuration / 1000.0).toInt()
        val dashSamples = dotSamples * 3
        val symbolGapSamples = dotSamples
        val letterGapSamples = dotSamples * 3
        val wordGapSamples = dotSamples * 7

        val soundData = mutableListOf<Short>()

        for (char in morseCode) {
            when (char) {
                '.' -> {
                    soundData.addAll(generateTone(dotSamples))
                    soundData.addAll(generateSilence(symbolGapSamples))
                }
                '-' -> {
                    soundData.addAll(generateTone(dashSamples))
                    soundData.addAll(generateSilence(symbolGapSamples))
                }
                ' ' -> {
                    soundData.addAll(generateSilence(letterGapSamples))
                }
                '/' -> {
                    soundData.addAll(generateSilence(wordGapSamples))
                }
            }
        }

        soundData.addAll(generateSilence(wordGapSamples * 2))

        return soundData.toShortArray()
    }

    private fun generateTone(numSamples: Int): List<Short> {
        return List(numSamples) { i ->
            (Short.MAX_VALUE * sin(2.0 * PI * frequency * i / sampleRate)).toInt().toShort()
        }
    }

    private fun generateSilence(numSamples: Int): List<Short> {
        return List(numSamples) { 0.toShort() }
    }

    fun release() {
        stopPlaying()
        playerScope.cancel()
        audioTrack?.release()
        audioTrack = null
    }
}