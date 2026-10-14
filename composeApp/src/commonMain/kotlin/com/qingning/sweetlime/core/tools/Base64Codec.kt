package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.NativeText

/**
 * Base64 编解码（UTF-8）。
 *
 * 自己实现而不是用标准库：Kotlin 的 `Base64` 在 common 源集里还是实验 API，
 * 而这段逻辑总共几十行、边界清晰，自己实现反而更稳，也不会被将来的 API 变动影响。
 *
 * 解码时做了容错：忽略空白与换行，同时接受 URL-safe 的 `-` / `_`，
 * 这样从别处复制来的 Base64（常带换行、或 URL 变体）也能直接解。
 */
object Base64Codec {

    private const val TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    /** 文本 → Base64（UTF-8）。空串返回空串。 */
    fun encode(text: String): String {
        if (text.isEmpty()) return ""
        // Android arm64 上走 C++；拿不到原生结果就回落下面那份 Kotlin 实现。
        NativeText.base64Encode(text.encodeToByteArray())?.let { return it }
        return encodeKotlin(text)
    }

    /** Kotlin 版实现（兜底 + 对拍参照）。 */
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

    /**
     * Base64 → 文本（UTF-8）。格式不合法时返回 null。
     *
     * 允许缺省 padding，也允许中间插换行 —— 这些在别处复制来的 Base64 里很常见。
     */
    fun decode(text: String): String? {
        if (NativeText.available) {
            // 原生只做「Base64 → 字节」；UTF-8 合法性交回 Kotlin 判断，两边结论一致。
            val bytes = NativeText.base64Decode(text) ?: return null
            return runCatching { bytes.decodeToString() }.getOrNull()
        }
        return decodeKotlin(text)
    }

    /** Kotlin 版实现（兜底 + 对拍参照）。 */
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
