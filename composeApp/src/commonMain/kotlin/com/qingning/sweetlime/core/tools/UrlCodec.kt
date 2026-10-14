package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.NativeText

/**
 * URL 百分号编码 / 解码（UTF-8）。
 *
 * 和 [Base64Codec] 一样自己实现：common 源集里没有现成的、语义又刚好的标准库
 * （`java.net.URLEncoder` 只在 JVM 侧，而且它把空格编成 `+`，是「表单」那套，
 * 放进 URL 路径里是错的）。
 *
 * 这一份按 **encodeURIComponent** 的规则来：
 *   * 不转义的只有 `A-Z a-z 0-9` 和 `- _ . ! ~ * ' ( )`
 *   * 其余一律按 UTF-8 逐字节转成 `%XX`（空格是 `%20`，不是 `+`）
 *
 * 所以「编码 → 解码」是严格可逆的；解码那边额外容忍两点外部输入常见的情况：
 *   * `+` 当作空格（从 query string 里复制出来的值基本都这样）
 *   * `%` 后面的大小写十六进制都认
 */
object UrlCodec {

    /** 不需要转义的字符（RFC 3986 的 unreserved + encodeURIComponent 额外保留的那几个）。 */
    private const val SAFE = "-_.!~*'()"

    private const val HEX = "0123456789ABCDEF"

    /** 文本 → URL 编码。空串返回空串。 */
    fun encode(text: String): String {
        if (text.isEmpty()) return ""
        NativeText.urlEncode(text.encodeToByteArray())?.let { return it }
        return encodeKotlin(text)
    }

    /** Kotlin 版实现（兜底 + 对拍参照）。 */
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

    /**
     * URL 编码 → 文本。`%` 后面不是两位十六进制时返回 null（调用点会给出提示）。
     *
     * `+` 按空格处理；其余字符原样保留，所以把一整条 URL 丢进来也不会被破坏。
     */
    fun decode(text: String): String? {
        if (text.isEmpty()) return ""
        if (NativeText.available) {
            val bytes = NativeText.urlDecodeBytes(text) ?: return null
            return runCatching { bytes.decodeToString() }.getOrNull()
        }
        return decodeKotlin(text)
    }

    /** Kotlin 版实现（兜底 + 对拍参照）。 */
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
                    // 非 ASCII 字符原样保留（例如把一条已经部分是中文的 URL 丢进来）。
                    //
                    // 注意：增补平面字符（emoji 等）在 Kotlin 里是**两个 Char**，
                    // 必须把代理对合成一个码点再编码 —— 单独编码一个「半个代理」
                    // 只会得到一个替换字符，于是 🎉 会变成两个 ??。
                    // （C++ 那份是按码点走的，这里跟它对齐。）
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