package com.qingning.sweetlime.core.tools

object QqNameColor {

    const val DEFAULT_ALPHA = 255
    const val DEFAULT_RED = 255
    const val DEFAULT_GREEN = 0
    const val DEFAULT_BLUE = 0

    const val CHANNEL_MIN = 0
    const val CHANNEL_MAX = 255

    private val TRAILING_PUNCT = Regex("[\\p{P}\\p{S}]+$")

    private const val ISOLATE = '\u2067'

    private const val LRI = '\u2066'

    fun colorTag(alpha: Int, red: Int, green: Int, blue: Int): String = buildString(7) {
        append("<&")
        append(channelChar(alpha))
        append(channelChar(red))
        append(channelChar(green))
        append(channelChar(blue))
        append('>')
    }

    fun splitTrailingPuncts(tail: String): Pair<String, String> {
        val match = TRAILING_PUNCT.find(tail) ?: return "" to tail
        return match.value to tail.substring(0, match.range.first)
    }

    fun build(
        nickname: String,
        tail: String,
        alpha: Int = DEFAULT_ALPHA,
        red: Int = DEFAULT_RED,
        green: Int = DEFAULT_GREEN,
        blue: Int = DEFAULT_BLUE,
        useColor: Boolean = true,
        useTail: Boolean = true,
    ): String {
        val name = nickname.trim()

        val hasTail = useTail && tail.isNotBlank()

        if (!useColor && !hasTail) return name

        val tailBody = if (hasTail) {
            val (puncts, body) = splitTrailingPuncts(tail.trim())
            puncts.reversed() + body
        } else {
            ""
        }

        return buildString {
            if (useColor) append(colorTag(alpha, red, green, blue))
            append(name)
            append(ISOLATE)
            append(tailBody)
            append(LRI)
        }
    }

    private fun channelChar(value: Int): Char = value.coerceIn(CHANNEL_MIN, CHANNEL_MAX).toChar()
}