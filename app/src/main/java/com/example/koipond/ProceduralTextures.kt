package com.example.koipond

import android.opengl.GLES30
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Generates tileable procedural textures in CPU memory once during GL initialization
 * so shaders have rich multi-octave noise, water normal maps, and koi pigment masks
 * with zero bitmap file dependencies.
 */
object ProceduralTextures {

    /**
     * Creates a 256x256 RGBA8 seamless tileable texture:
     * R = Perlin-like low-frequency FBM noise
     * G = Voronoi cellular pebbles/caustic base
     * B = High-frequency grain / sand sparkle
     * A = Secondary phase-shifted FBM for normal-map wave interference
     */
    fun createNoiseAndCausticsTexture(size: Int = 256): Int {
        val buffer = ByteBuffer.allocateDirect(size * size * 4).order(ByteOrder.nativeOrder())

        for (y in 0 until size) {
            val v = y.toFloat() / size.toFloat()
            for (x in 0 until size) {
                val u = x.toFloat() / size.toFloat()

                val fbm1 = tileableFbm(u, v, 4, 13)
                val vor = tileableVoronoi(u, v, 8, 41)
                val grain = hash2D((x * 7) and 255, (y * 7) and 255, 97)
                val fbm2 = tileableFbm(u + 0.37f, v + 0.61f, 4, 59)

                buffer.put((fbm1 * 255f).toInt().coerceIn(0, 255).toByte())
                buffer.put((vor * 255f).toInt().coerceIn(0, 255).toByte())
                buffer.put((grain * 255f).toInt().coerceIn(0, 255).toByte())
                buffer.put((fbm2 * 255f).toInt().coerceIn(0, 255).toByte())
            }
        }
        buffer.position(0)

        val texIds = IntArray(1)
        GLES30.glGenTextures(1, texIds, 0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texIds[0])
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            size,
            size,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            buffer
        )
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT)
        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        return texIds[0]
    }

    private fun tileableFbm(u: Float, v: Float, octaves: Int, seed: Int): Float {
        var sum = 0f
        var amp = 0.5f
        var freq = 4
        var norm = 0f
        for (i in 0 until octaves) {
            sum += amp * tileableValueNoise(u, v, freq, seed + i * 19)
            norm += amp
            amp *= 0.5f
            freq *= 2
        }
        return (sum / norm).coerceIn(0f, 1f)
    }

    private fun tileableValueNoise(u: Float, v: Float, period: Int, seed: Int): Float {
        val px = u * period
        val py = v * period
        val ix = floor(px).toInt()
        val iy = floor(py).toInt()
        val fx = px - ix
        val fy = py - iy

        val sx = fx * fx * (3f - 2f * fx)
        val sy = fy * fy * (3f - 2f * fy)

        val x0 = ((ix % period) + period) % period
        val x1 = (x0 + 1) % period
        val y0 = ((iy % period) + period) % period
        val y1 = (y0 + 1) % period

        val n00 = hash2D(x0, y0, seed)
        val n10 = hash2D(x1, y0, seed)
        val n01 = hash2D(x0, y1, seed)
        val n11 = hash2D(x1, y1, seed)

        val nx0 = n00 + sx * (n10 - n00)
        val nx1 = n01 + sx * (n11 - n01)
        return nx0 + sy * (nx1 - nx0)
    }

    private fun tileableVoronoi(u: Float, v: Float, cells: Int, seed: Int): Float {
        val px = u * cells
        val py = v * cells
        val ix = floor(px).toInt()
        val iy = floor(py).toInt()
        val fx = px - ix
        val fy = py - iy

        var minDist = 8f
        for (dy in -1..1) {
            for (dx in -1..1) {
                val cx = (((ix + dx) % cells) + cells) % cells
                val cy = (((iy + dy) % cells) + cells) % cells
                val rx = dx + hash2D(cx, cy, seed) - fx
                val ry = dy + hash2D(cx, cy, seed + 101) - fy
                val d = sqrt(rx * rx + ry * ry)
                if (d < minDist) minDist = d
            }
        }
        return (1f - minDist.coerceIn(0f, 1f))
    }

    private fun hash2D(x: Int, y: Int, seed: Int): Float {
        var n = x * 374761393 + y * 668265263 + seed * 1442695041
        n = (n xor (n ushr 13)) * 1274126177
        n = n xor (n ushr 16)
        return (n and 0x7fffffff).toFloat() / 2147483647.0f
    }
}
