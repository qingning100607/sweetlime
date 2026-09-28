package com.qingning.sweetlime.core.mapping

/**
 * 把 Unicode 码点转成 String。
 *
 * 关键点：Kotlin 的 Char 只是一个 UTF-16 码元，`Int.toChar()` 会**截断高 16 位**。
 * 花体字符（数学字母数字符号区 U+1D400 起）都在 BMP 之外，直接 toChar() 会得到
 * 一堆毫不相干的汉字/韩文（例如 U+1D416 会变成 U+D416「퓈」）。
 * 所以必须在 BMP 之外手动拼出代理对。
 */
internal fun codePointToString(codePoint: Int): String {
    if (codePoint <= 0xFFFF) return codePoint.toChar().toString()
    val offset = codePoint - 0x10000
    val high = 0xD800 + (offset shr 10)
    val low = 0xDC00 + (offset and 0x3FF)
    return "" + high.toChar() + low.toChar()
}

/** 逐字符映射，映射不到的字符原样保留。 */
internal fun mapEach(input: String, mapper: (Char) -> String?): String =
    buildString(input.length) {
        input.forEach { char ->
            append(mapper(char) ?: char.toString())
        }
    }

/**
 * 构造一个「数学字母符号区」映射器。
 *
 * Unicode 的 Mathematical Alphanumeric Symbols 区块大体是连续的，
 * 但个别码位是未分配的（比如脚本体的小写 h），需要用 Letterlike Symbols
 * 里的字符补位，这就是 [exceptions] 存在的原因。
 */
internal fun mathRange(
    upper: Int,
    lower: Int,
    digits: Int? = null,
    exceptions: Map<Char, String> = emptyMap(),
): (Char) -> String? = { char ->
    exceptions[char] ?: when {
        char in 'A'..'Z' -> codePointToString(upper + (char - 'A'))
        char in 'a'..'z' -> codePointToString(lower + (char - 'a'))
        char in '0'..'9' && digits != null -> codePointToString(digits + (char - '0'))
        else -> null
    }
}
