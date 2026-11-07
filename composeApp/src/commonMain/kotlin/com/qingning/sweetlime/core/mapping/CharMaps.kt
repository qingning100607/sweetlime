package com.qingning.sweetlime.core.mapping

internal fun codePointToString(codePoint: Int): String {
    if (codePoint <= 0xFFFF) return codePoint.toChar().toString()
    val offset = codePoint - 0x10000
    val high = 0xD800 + (offset shr 10)
    val low = 0xDC00 + (offset and 0x3FF)
    return "" + high.toChar() + low.toChar()
}

internal fun mapEach(input: String, mapper: (Char) -> String?): String =
    buildString(input.length) {
        input.forEach { char ->
            append(mapper(char) ?: char.toString())
        }
    }

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
