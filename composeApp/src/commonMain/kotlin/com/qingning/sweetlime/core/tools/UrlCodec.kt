package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.NativeText

object UrlCodec {

    private const val SAFE = "-_.!~*'()"

    private const val HEX = "0123456789ABCDEF"

    fun encode(text: String): String {
        if (text.isEmpty()) return ""
        NativeText.urlEncode(text.encodeToByteArray())?.let { return it }
        return encodeKotlin(text)
    }

    internal fun encodeKotlin(text: String): String {
        if (text.isEmpty()) return ""
        val out = StringBuilder(text.length * 3)
        for (byte in text.encodeToByteArray()) {
            val value = byte.toInt() and 0xFF
            val ch = value.toChar()
            if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' || SAFE.indexOf(ch) >= 0) {
                out.append(ch)
            } else {
                out.append('%')
                out.append(HEX[value shr 4])
                out.append(HEX[value and 0x0F])
            }
        }
        return out.toString()
    }

    fun decode(text: String): String? {
        if (text.isEmpty()) return ""
        if (NativeText.available) {
            val bytes = NativeText.urlDecodeBytes(text) ?: return null
            return runCatching { bytes.decodeToString() }.getOrNull()
        }
        return decodeKotlin(text)
    }

    internal fun decodeKotlin(text: String): String? {
        if (text.isEmpty()) return ""
        val bytes = ArrayList<Byte>(text.length)
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            when {
                ch == '%' -> {
                    if (i + 2 >= text.length) return null
                    val hi = hexValue(text[i + 1]) ?: return null
                    val lo = hexValue(text[i + 2]) ?: return null
                    bytes.add(((hi shl 4) or lo).toByte())
                    i += 3
                }

                ch == '+' -> {
                    bytes.add(' '.code.toByte())
                    i++
                }

                else -> {

                    if (ch.isHighSurrogate() && i + 1 < text.length &&
                        text[i + 1].isLowSurrogate()
                    ) {
                        for (b in text.substring(i, i + 2).encodeToByteArray()) bytes.add(b)
                        i += 2
                    } else {
                        for (b in ch.toString().encodeToByteArray()) bytes.add(b)
                        i++
                    }
                }
            }
        }
        return runCatching { bytes.toByteArray().decodeToString() }.getOrNull()
    }

    private fun hexValue(c: Char): Int? = when (c) {
        in '0'..'9' -> c - '0'
        in 'a'..'f' -> c - 'a' + 10
        in 'A'..'F' -> c - 'A' + 10
        else -> null
    }
}