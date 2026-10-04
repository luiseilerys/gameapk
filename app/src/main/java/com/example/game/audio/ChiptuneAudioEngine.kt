package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Music tracks matching the game's biomes and states.
 */
enum class MusicTrack {
    NONE,
    OVERWORLD,
    BATTLE,
    BOSS,
    DUNGEON
}

/**
 * Procedural Chiptune Audio Engine emulating 16-bit SNES sound chip architectures:
 * - Pulse/Square wave generators with duty cycles
 * - Triangle bass synthesizer
 * - Noise generator for 16-bit percussion
 * - Multi-voice real-time music sequencer and instant SFX playback
 */
class ChiptuneAudioEngine(private val scope: CoroutineScope) {
    private val sampleRate = 22050
    private var musicJob: Job? = null
    var currentTrack: MusicTrack = MusicTrack.NONE
        private set

    var isSoundEnabled: Boolean = true
    var isMusicEnabled: Boolean = true
    var sfxVolume: Float = 0.8f
    var musicVolume: Float = 0.6f

    // Reusable pre-rendered SFX PCM buffers for instantaneous zero-latency playback
    private val sfxBuffers = mutableMapOf<String, ShortArray>()

    init {
        scope.launch(Dispatchers.Default) {
            pregenerateSfx()
        }
    }

    private fun pregenerateSfx() {
        sfxBuffers["footstep"] = generateFootstep()
        sfxBuffers["attack"] = generateAttackSfx()
        sfxBuffers["damage"] = generateDamageSfx()
        sfxBuffers["pickup"] = generatePickupSfx()
        sfxBuffers["chest"] = generateChestSfx()
        sfxBuffers["levelup"] = generateLevelUpSfx()
        sfxBuffers["battlestart"] = generateBattleStartSfx()
        sfxBuffers["select"] = generateSelectSfx()
        sfxBuffers["defeat"] = generateDefeatSfx()
        sfxBuffers["flee"] = generateFleeSfx()
    }

    /**
     * Plays a precomputed retro sound effect.
     */
    fun playSfx(sfxKey: String) {
        if (!isSoundEnabled) return
        val buffer = sfxBuffers[sfxKey] ?: return

        scope.launch(Dispatchers.IO) {
            try {
                val track = createAudioTrack(buffer.size)
                // Apply volume
                val scaledBuffer = ShortArray(buffer.size) { i ->
                    (buffer[i] * sfxVolume).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }
                track.write(scaledBuffer, 0, scaledBuffer.size)
                track.play()
                val durationMs = (buffer.size * 1000L) / sampleRate
                delay(durationMs + 50)
                track.stop()
                track.release()
            } catch (e: Exception) {
                // AudioTrack error safeguard
            }
        }
    }

    /**
     * Start playing looping background music track.
     */
    fun playMusic(track: MusicTrack) {
        if (currentTrack == track) return
        currentTrack = track
        musicJob?.cancel()

        if (!isMusicEnabled || track == MusicTrack.NONE) return

        musicJob = scope.launch(Dispatchers.IO) {
            when (track) {
                MusicTrack.OVERWORLD -> runOverworldMusicLoop()
                MusicTrack.BATTLE -> runBattleMusicLoop()
                MusicTrack.BOSS -> runBossMusicLoop()
                MusicTrack.DUNGEON -> runDungeonMusicLoop()
                MusicTrack.NONE -> {}
            }
        }
    }

    fun stopMusic() {
        currentTrack = MusicTrack.NONE
        musicJob?.cancel()
    }

    fun toggleMusic(enabled: Boolean) {
        isMusicEnabled = enabled
        if (!enabled) {
            musicJob?.cancel()
        } else if (currentTrack != MusicTrack.NONE) {
            val trackToResume = currentTrack
            currentTrack = MusicTrack.NONE
            playMusic(trackToResume)
        }
    }

    // ==========================================
    // PROCEDURAL MUSIC SEQUENCERS
    // ==========================================

    private suspend fun runOverworldMusicLoop() {
        // Melodic notes (frequencies in Hz)
        val melody = listOf(
            261.63f, 329.63f, 392.00f, 523.25f,
            440.00f, 392.00f, 349.23f, 329.63f,
            293.66f, 349.23f, 440.00f, 523.25f,
            493.88f, 392.00f, 440.00f, 523.25f
        )
        val bass = listOf(130.81f, 164.81f, 174.61f, 196.00f)

        while (scope.isActive && currentTrack == MusicTrack.OVERWORLD) {
            for (i in melody.indices) {
                if (!scope.isActive || currentTrack != MusicTrack.OVERWORLD) break
                val leadFreq = melody[i]
                val bassFreq = bass[i % bass.size]
                val durationMs = 180
                playChiptuneChord(leadFreq, bassFreq, durationMs, dutyCycle = 0.5f, noisePerc = (i % 2 == 1))
            }
        }
    }

    private suspend fun runBattleMusicLoop() {
        // Fast energetic driving pulse
        val battleNotes = listOf(
            220.00f, 220.00f, 261.63f, 293.66f,
            329.63f, 293.66f, 261.63f, 220.00f,
            196.00f, 220.00f, 246.94f, 293.66f,
            329.63f, 369.99f, 392.00f, 440.00f
        )

        while (scope.isActive && currentTrack == MusicTrack.BATTLE) {
            for (i in battleNotes.indices) {
                if (!scope.isActive || currentTrack != MusicTrack.BATTLE) break
                val freq = battleNotes[i]
                val bassFreq = 110.00f // Deep A2 bass
                val durationMs = 135
                playChiptuneChord(freq, bassFreq, durationMs, dutyCycle = 0.25f, noisePerc = true)
            }
        }
    }

    private suspend fun runBossMusicLoop() {
        // Heavy, ominous minor key theme
        val bossNotes = listOf(
            146.83f, 174.61f, 220.00f, 207.65f,
            146.83f, 174.61f, 233.08f, 220.00f,
            138.59f, 164.81f, 207.65f, 196.00f,
            146.83f, 220.00f, 293.66f, 349.23f
        )

        while (scope.isActive && currentTrack == MusicTrack.BOSS) {
            for (i in bossNotes.indices) {
                if (!scope.isActive || currentTrack != MusicTrack.BOSS) break
                val freq = bossNotes[i]
                val durationMs = 150
                playChiptuneChord(freq, freq * 0.5f, durationMs, dutyCycle = 0.125f, noisePerc = (i % 2 == 0))
            }
        }
    }

    private suspend fun runDungeonMusicLoop() {
        // Mysterious, ambient ruins chiptune
        val dungeonNotes = listOf(
            329.63f, 0f, 311.13f, 0f,
            293.66f, 349.23f, 329.63f, 0f,
            246.94f, 0f, 261.63f, 0f,
            293.66f, 0f, 329.63f, 246.94f
        )

        while (scope.isActive && currentTrack == MusicTrack.DUNGEON) {
            for (i in dungeonNotes.indices) {
                if (!scope.isActive || currentTrack != MusicTrack.DUNGEON) break
                val freq = dungeonNotes[i]
                val durationMs = 240
                if (freq > 0) {
                    playChiptuneChord(freq, freq * 0.5f, durationMs, dutyCycle = 0.5f, noisePerc = false)
                } else {
                    delay(durationMs.toLong())
                }
            }
        }
    }

    private suspend fun playChiptuneChord(
        leadFreq: Float,
        bassFreq: Float,
        durationMs: Int,
        dutyCycle: Float,
        noisePerc: Boolean
    ) {
        val totalSamples = (sampleRate * durationMs) / 1000
        val buffer = ShortArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toFloat() / sampleRate
            // Pulse lead
            val phaseLead = (t * leadFreq) % 1.0f
            val leadSample = if (phaseLead < dutyCycle) 0.4f else -0.4f

            // Triangle bass
            val phaseBass = (t * bassFreq) % 1.0f
            val bassSample = (4.0f * kotlin.math.abs(phaseBass - 0.5f) - 1.0f) * 0.35f

            // Noise percussion hit at start of note
            val noiseSample = if (noisePerc && i < totalSamples / 4) {
                ((Math.random() * 2.0 - 1.0) * 0.25f * (1.0f - (i.toFloat() / (totalSamples / 4)))).toFloat()
            } else 0f

            // Envelope (slight fade out)
            val envelope = (1.0f - (i.toFloat() / totalSamples) * 0.3f)
            val mixed = ((leadSample + bassSample + noiseSample) * envelope * musicVolume)
                .coerceIn(-1.0f, 1.0f)

            buffer[i] = (mixed * Short.MAX_VALUE).toInt().toShort()
        }

        try {
            val track = createAudioTrack(buffer.size)
            track.write(buffer, 0, buffer.size)
            track.play()
            delay(durationMs.toLong())
            track.stop()
            track.release()
        } catch (e: Exception) {
            delay(durationMs.toLong())
        }
    }

    // ==========================================
    // PROCEDURAL SFX GENERATORS
    // ==========================================

    private fun generateFootstep(): ShortArray {
        val count = (sampleRate * 0.05f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toFloat() / count
            val noise = (Math.random() * 2.0 - 1.0).toFloat()
            val amp = (1.0f - t) * 0.2f
            buf[i] = (noise * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateAttackSfx(): ShortArray {
        val count = (sampleRate * 0.15f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val freq = 800f * (1.0f - progress * 0.7f)
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.5f else -0.5f
            val noise = ((Math.random() * 2.0 - 1.0).toFloat()) * 0.4f
            val amp = (1.0f - progress)
            buf[i] = ((square * 0.6f + noise * 0.4f) * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateDamageSfx(): ShortArray {
        val count = (sampleRate * 0.22f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val noise = (Math.random() * 2.0 - 1.0).toFloat()
            val lowThud = sin(2.0 * PI * 80.0 * (i.toFloat() / sampleRate)).toFloat()
            val amp = (1.0f - progress)
            buf[i] = ((noise * 0.7f + lowThud * 0.3f) * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generatePickupSfx(): ShortArray {
        val count = (sampleRate * 0.16f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val freq = if (progress < 0.5f) 659.25f else 1046.50f // E5 to C6
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.45f else -0.45f
            val amp = 1.0f - (progress % 0.5f) * 1.2f
            buf[i] = (square * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateChestSfx(): ShortArray {
        val count = (sampleRate * 0.36f).toInt()
        val buf = ShortArray(count)
        val freqs = listOf(523.25f, 659.25f, 783.99f, 1046.50f) // C5, E5, G5, C6
        for (i in 0 until count) {
            val section = (i.toFloat() / count * freqs.size).toInt().coerceIn(0, freqs.size - 1)
            val freq = freqs[section]
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.5f else -0.5f
            buf[i] = (square * 0.7f * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateLevelUpSfx(): ShortArray {
        val count = (sampleRate * 0.65f).toInt()
        val buf = ShortArray(count)
        val notes = listOf(440f, 554.37f, 659.25f, 880f)
        for (i in 0 until count) {
            val section = (i.toFloat() / count * notes.size).toInt().coerceIn(0, notes.size - 1)
            val freq = notes[section]
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.35f) 0.5f else -0.5f
            buf[i] = (square * 0.75f * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateBattleStartSfx(): ShortArray {
        val count = (sampleRate * 0.45f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            // Rapid upward arpeggio sweep
            val freq = 200f + progress * 800f
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.5f else -0.5f
            buf[i] = (square * 0.8f * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateSelectSfx(): ShortArray {
        val count = (sampleRate * 0.06f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val phase = (i.toFloat() / sampleRate * 900f) % 1.0f
            val square = if (phase < 0.5f) 0.4f else -0.4f
            buf[i] = (square * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateDefeatSfx(): ShortArray {
        val count = (sampleRate * 0.35f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val freq = 400f * (1.0f - progress * 0.75f)
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.5f else -0.5f
            val noise = ((Math.random() * 2.0 - 1.0).toFloat()) * 0.3f
            val amp = 1.0f - progress
            buf[i] = ((square * 0.7f + noise * 0.3f) * amp * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun generateFleeSfx(): ShortArray {
        val count = (sampleRate * 0.25f).toInt()
        val buf = ShortArray(count)
        for (i in 0 until count) {
            val progress = i.toFloat() / count
            val freq = 800f * (1.0f - progress * 0.6f)
            val phase = (i.toFloat() / sampleRate * freq) % 1.0f
            val square = if (phase < 0.5f) 0.4f else -0.4f
            buf[i] = (square * (1f - progress) * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun createAudioTrack(bufferSize: Int): AudioTrack {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } else {
            @Suppress("DEPRECATION")
            AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2,
                AudioTrack.MODE_STREAM
            )
        }
    }
}
