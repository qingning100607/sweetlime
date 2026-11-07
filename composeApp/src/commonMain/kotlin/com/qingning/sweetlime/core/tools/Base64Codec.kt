package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.NativeText

object Base64Codec {

    private const val TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(text: String): String {
        if (text.isEmpty()) return ""

        NativeText.base64Encode(text.encodeToByteArray())?.let { return it }
        return encodeKotlin(text)
    }

    internal fun encodeKotlin(text: String): String {
        if (text.isEmpty()) return ""
        val bytes = text.encodeToByteArray()
        val out = StringBuilder((bytes.size + 2) / 3 * 4)
        var i = 0
        while (i < bytes.size) {
            val b0 = bytes[i].toInt() and 0xFF
            val b1 = if (i + 1 < bytes.size) bytes[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < bytes.size) bytes[i + 2].toInt() and 0xFF else 0
            out.append(TABLE[b0 shr 2])
            out.append(TABLE[((b0 and 0x03) shl 4) or (b1 shr 4)])
            out.append(if (i + 1 < bytes.size) TABLE[((b1 and 0x0F) shl 2) or (b2 shr 6)] else '=')
            out.append(if (i + 2 < bytes.size) TABLE[b2 and 0x3F] else '=')
            i += 3
        }
        return out.toString()
    }

    fun decode(text: String): String? {
        if (NativeText.available) {

            val bytes = NativeText.base64Decode(text) ?: return null
            return runCatching { bytes.decodeToString() }.getOrNull()
        }
        return decodeKotlin(text)
    }

    internal fun decodeKotlin(text: String): String? {
        val clean = text.filterNot { it.isWhitespace() }
            .replace('-', '+')
            .replace('_', '/')
        if (clean.isEmpty()) return ""
        val data = clean.trimEnd('=')
        val out = ArrayList<Byte>(data.length * 3 / 4 + 2)
        var buffer = 0
        var bits = 0
        for (ch in data) {
            val value = TABLE.indexOf(ch)
            if (value < 0) return null
            buffer = (buffer shl 6) or value
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.add(((buffer shr bits) and 0xFF).toByte())
            }
        }
        return runCatching { out.toByteArray().decodeToString() }.getOrNull()
    }
}
