package com.example.game.world

import kotlin.math.floor

/**
 * 2D Simplex Noise implementation in pure Kotlin for smooth, coherent procedural terrain and biome distribution.
 * Highly optimized, deterministic based on world seed.
 */
class SimplexNoise(seed: Long) {
    private val perm = IntArray(512)
    private val permMod12 = IntArray(512)

    init {
        val p = IntArray(256) { it }
        // Deterministic shuffle with seed
        var s = seed
        for (i in 255 downTo 1) {
            s = (s * 6364136223846793005L + 1442695040888963407L)
            val j = ((s ushr 32) and 0x7FFFFFFF).toInt() % (i + 1)
            val temp = p[i]
            p[i] = p[j]
            p[j] = temp
        }
        for (i in 0 until 512) {
            perm[i] = p[i and 255]
            permMod12[i] = perm[i] % 12
        }
    }

    private val grad3 = arrayOf(
        floatArrayOf(1f, 1f, 0f), floatArrayOf(-1f, 1f, 0f), floatArrayOf(1f, -1f, 0f), floatArrayOf(-1f, -1f, 0f),
        floatArrayOf(1f, 0f, 1f), floatArrayOf(-1f, 0f, 1f), floatArrayOf(1f, 0f, -1f), floatArrayOf(-1f, 0f, -1f),
        floatArrayOf(0f, 1f, 1f), floatArrayOf(0f, -1f, 1f), floatArrayOf(0f, 1f, -1f), floatArrayOf(0f, -1f, -1f)
    )

    private val f2 = 0.5f * (kotlin.math.sqrt(3.0f) - 1.0f)
    private val g2 = (3.0f - kotlin.math.sqrt(3.0f)) / 6.0f

    fun eval(xin: Float, yin: Float): Float {
        var n0 = 0f
        var n1 = 0f
        var n2 = 0f

        val s = (xin + yin) * f2
        val i = floor(xin + s).toInt()
        val j = floor(yin + s).toInt()
        val t = (i + j) * g2
        val x0 = i - t
        val y0 = j - t
        val x0f = xin - x0
        val y0f = yin - y0

        val i1: Int
        val j1: Int
        if (x0f > y0f) {
            i1 = 1
            j1 = 0
        } else {
            i1 = 0
            j1 = 1
        }

        val x1f = x0f - i1 + g2
        val y1f = y0f - j1 + g2
        val x2f = x0f - 1.0f + 2.0f * g2
        val y2f = y0f - 1.0f + 2.0f * g2

        val ii = i and 255
        val jj = j and 255
        val gi0 = permMod12[ii + perm[jj]]
        val gi1 = permMod12[ii + i1 + perm[jj + j1]]
        val gi2 = permMod12[ii + 1 + perm[jj + 1]]

        var t0 = 0.5f - x0f * x0f - y0f * y0f
        if (t0 > 0) {
            t0 *= t0
            n0 = t0 * t0 * (grad3[gi0][0] * x0f + grad3[gi0][1] * y0f)
        }

        var t1 = 0.5f - x1f * x1f - y1f * y1f
        if (t1 > 0) {
            t1 *= t1
            n1 = t1 * t1 * (grad3[gi1][0] * x1f + grad3[gi1][1] * y1f)
        }

        var t2 = 0.5f - x2f * x2f - y2f * y2f
        if (t2 > 0) {
            t2 *= t2
            n2 = t2 * t2 * (grad3[gi2][0] * x2f + grad3[gi2][1] * y2f)
        }

        // Return normalized [-1.0, 1.0] to [0.0, 1.0]
        val result = 70.0f * (n0 + n1 + n2)
        return ((result + 1.0f) * 0.5f).coerceIn(0.0f, 1.0f)
    }

    /**
     * Fractal Brownian Motion with multiple octaves for natural landscapes.
     */
    fun fbm(x: Float, y: Float, octaves: Int = 4, persistence: Float = 0.5f, lacunarity: Float = 2.0f): Float {
        var total = 0f
        var frequency = 1f
        var amplitude = 1f
        var maxValue = 0f

        for (i in 0 until octaves) {
            total += eval(x * frequency, y * frequency) * amplitude
            maxValue += amplitude
            amplitude *= persistence
            frequency *= lacunarity
        }

        return total / maxValue
    }
}
