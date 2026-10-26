package com.qingning.sweetlime.core.tools

object QqNameColor {

    
    const val DEFAULT_ALPHA = 255
    const val DEFAULT_RED = 255
    const val DEFAULT_GREEN = 0
    const val DEFAULT_BLUE = 0

    /** 通道取值范围（滑块也是这个范围）。 */
    const val CHANNEL_MIN = 0
    const val CHANNEL_MAX = 255

    
    private val TRAILING_PUNCT = Regex("[\\p{P}\\p{S}]+$")

    /** U+2067：把尾巴隔离起来。 */
    private const val ISOLATE = '\u2067'

    /** U+2066 LRI：整串的收尾符（用户实例里那个 ⁦）。 */
    private const val LRI = '\u2066'

    /** 颜色标记：`<&` + 四个「码点 = 通道值」的字符 + `>`。 */
    fun colorTag(alpha: Int, red: Int, green: Int, blue: Int): String = buildString(7) {
        append("<&")
        append(channelChar(alpha))
        append(channelChar(red))
        append(channelChar(green))
        append(channelChar(blue))
        append('>')
    }

    /**
     * 把尾巴拆成「结尾的标点串」和「剩下的正文」。
     *
     * @return `(标点串, 正文)`；结尾没有标点 / 符号时是 `("", 整个尾巴)`。
     */
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
        // 尾巴算不算「有」：开关开着、而且里面不是纯空白（纯空格等于没填）。
        val hasTail = useTail && tail.isNotBlank()
        // 两个都不要时，直接给昵称本身，不往里面塞那些看不见的控制字符。
        if (!useColor && !hasTail) return name

        // 首尾空白先去掉：昵称和尾巴之间本来就会被拼得紧贴，
        // 用户多打（或从别处粘过来带上）的空格会变成一道看得见的空档，属于多余。
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

    /** 通道值 → 字符：值就是码点（0..255 全在 Latin-1 区，一个 UTF-16 单元放得下）。 */
    private fun channelChar(value: Int): Char = value.coerceIn(CHANNEL_MIN, CHANNEL_MAX).toChar()
}