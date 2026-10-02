package com.qingning.sweetlime.core.tools

import kotlin.math.abs
import kotlin.math.sin

/**
 * 纯 Kotlin 的 MD5（RFC 1321）。
 *
 * 为什么自己写：commonMain 里没有 `java.security.MessageDigest`，
 * 而 MD5 一共就几十行，自己实现一份能让 Android 侧和其它端行为完全一致，也能直接跑单测。
 *
 * 注意：MD5 早就不安全了，这里只适合「校验文件/文本有没有被改过」这类用途，
 * 别拿它当密码散列。
 */
object Md5 {

    /** 16 进制小写字符表。 */
    private const val HEX = "0123456789abcdef"

    /** 四轮里每步的左移位数（RFC 1321 的 s 表）。 */
    private val SHIFTS = intArrayOf(
        7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21,
    )

    /**
     * 64 个加法常量 = floor(abs(sin(i + 1)) × 2^32)。
     *
     * RFC 就是按这个式子定义的，所以这里直接算，不手抄常表 —— 抄错一位整条结果就全错，
     * 而算式有单测盯着。（Math.sin 是 IEEE 双精度，算出来和官方常表逐位相同。）
     */
    private val K = IntArray(64) { i ->
        (abs(sin((i + 1).toDouble())) * 4294967296.0).toLong().toInt()
    }

    /** 32 位小写十六进制 MD5。 */
    fun hash(text: String): String = hash(text.encodeToByteArray())

    /** 32 位小写十六进制 MD5（对原始字节，不做任何编码转换）。 */
    fun hash(bytes: ByteArray): String {
        // 1) 补位：先塞一个 0x80，再补 0 到「长度 % 64 == 56」，最后 8 字节放小端的比特长度。
        //    这个写法把「补 0 后还要不要多一个块」两种情况一起算了。
        val bitLength = bytes.size.toLong() * 8L
        val padded = ((bytes.size + 8) / 64 + 1) * 64
        val msg = ByteArray(padded)
        bytes.copyInto(msg)
        msg[bytes.size] = 0x80.toByte()
        for (i in 0 until 8) {
            msg[padded - 8 + i] = ((bitLength ushr (8 * i)) and 0xFF).toByte()
        }

        // 2) 寄存器初值（RFC 里那四个魔数），按小端把每个 64 字节块读成 16 个 Int。
        var a0 = 0x67452301
        var b0 = -0x10325477 // 0xefcdab89
        var c0 = -0x67452302 // 0x98badcfe
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

        // 3) 四个寄存器各自按小端写成 8 个十六进制字符。
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

    /** 「16 位 MD5」= 32 位里中间那 16 个字符，很多老系统只认这个。 */
    fun hash16(text: String): String = hash(text).substring(8, 24)

    private fun rotateLeft(value: Int, bits: Int): Int = (value shl bits) or (value ushr (32 - bits))
}
