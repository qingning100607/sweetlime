package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.NativeText
import kotlin.math.abs
import kotlin.math.sin

object Md5 {

    private const val HEX = "0123456789abcdef"

    private val SHIFTS = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
    )

    private val K = IntArray(64) { i ->
        (abs(sin((i + 1).toDouble())) * 4294967296.0).toLong().toInt()
    }

    fun hash(text: String): String = hash(text.encodeToByteArray())

    fun hash(bytes: ByteArray): String {

        NativeText.md5Hex(bytes)?.let { return it }
        return hashKotlin(bytes)
    }

    internal fun hashKotlin(bytes: ByteArray): String {

        val bitLength = bytes.size.toLong() * 8L
        val padded = ((bytes.size + 8) / 64 + 1) * 64
        val msg = ByteArray(padded)
        bytes.copyInto(msg)
        msg[bytes.size] = 0x80.toByte()
        for (i in 0 until 8) {
            msg[padded - 8 + i] = ((bitLength ushr (8 * i)) and 0xFF).toByte()
        }

        var a0 = 0x67452301
        var b0 = -0x10325477
        var c0 = -0x67452302
        var d0 = 0x10325476
        val words = IntArray(16)
        var offset = 0
        while (offset < padded) {
            for (i in 0 until 16) {
                val p = offset + i * 4
                words[i] = (msg[p].toInt() and 0xFF) or
                    ((msg[p + 1].toInt() and 0xFF) shl 8) or
                    ((msg[p + 2].toInt() and 0xFF) shl 16) or
                    ((msg[p + 3].toInt() and 0xFF) shl 24)
            }
            var a = a0
            var b = b0
            var c = c0
            var d = d0
            for (i in 0 until 64) {
                val f: Int
                val g: Int
                when {
                    i < 16 -> {
                        f = (b and c) or (b.inv() and d)
                        g = i
                    }
                    i < 32 -> {
                        f = (d and b) or (d.inv() and c)
                        g = (5 * i + 1) % 16
                    }
                    i < 48 -> {
                        f = b xor c xor d
                        g = (3 * i + 5) % 16
                    }
                    else -> {
                        f = c xor (b or d.inv())
                        g = (7 * i) % 16
                    }
                }
                val old = d
                d = c
                c = b
                b += rotateLeft(a + f + K[i] + words[g], SHIFTS[i])
                a = old
            }
            a0 += a
            b0 += b
            c0 += c
            d0 += d
            offset += 64
        }

        val out = StringBuilder(32)
        for (v in intArrayOf(a0, b0, c0, d0)) {
            for (i in 0 until 4) {
                val byte = v ushr (8 * i)
                out.append(HEX[(byte ushr 4) and 0x0F])
                out.append(HEX[byte and 0x0F])
            }
        }
        return out.toString()
    }

    fun hash16(text: String): String = hash(text).substring(8, 24)

    private fun rotateLeft(value: Int, bits: Int): Int = (value shl bits) or (value ushr (32 - bits))
}
