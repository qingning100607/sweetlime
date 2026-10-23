package com.qingning.sweetlime.core.tools

/**
 * QQ 群昵称「美化串」的生成。
 *
 * 1:1 搬自上游网页 `rwgtzs.github.io/qq-name-beautify` 的 `_doCopy()`，规则没改。
 * 产出长这样（`␣` 处都是**不可见字符**，肉眼看只有昵称和尾巴）：
 *
 * ```
 * <&ÿÿ␀␀>昵称␠␡尾巴␡␍
 * ```
 *
 * 结构分三段，缺一段 QQ 那边的效果就不对：
 *
 * 1. **颜色标记**：`<&` + 四个字符 + `>`。每个字符的**码点**就是对应通道的值
 *    （顺序是 alpha、red、green、blue，各 0..255）—— QQ 读这个标记给昵称上色；
 * 2. **昵称**本体；
 * 3. **三个控制字符**：
 *      免得 QQ 把尾巴当成昵称的一部分去做方向 / 样式处理；
 *    - `U+202D` LEFT-TO-RIGHT OVERRIDE：把整串方向钉成从左到右，
 *      否则上面的隔离符在某些客户端上会把整行显示方向搞乱。
 *
 * 另外尾巴还有个上游自己的小动作：结尾**连续**的标点 / 符号会被切下来、倒序，
 * 再拼到尾巴最前面（`喵~` → `~喵`）。这是上游特意做的（让符号贴昵称更紧），
 * 不是笔误，所以搬过来时按原样保留。
 */
object QqNameColor {

    /** 颜色通道默认值：和上游一致 —— 不透明的红。 */
    const val DEFAULT_ALPHA = 255
    const val DEFAULT_RED = 255
    const val DEFAULT_GREEN = 0
    const val DEFAULT_BLUE = 0

    /** 通道取值范围（滑块也是这个范围）。 */
    const val CHANNEL_MIN = 0
    const val CHANNEL_MAX = 255

    /** 尾巴结尾的「标点 / 符号」连续串。`\p{P}` 标点、`\p{S}` 符号，与上游正则一致。 */
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

    /**
     * 生成完整的美化串。
     *
     * [nickname] 会去掉首尾空白（对齐上游的 `value.trim()`）；
     * [tail] 会去掉**首尾**空白（上游不 trim；实测尾巴开头多一个空格会在名字和尾巴之间
     *
     * [useColor] / [useTail] 是两个**可以独立开关**的部分 —— 上游网页只能一起用，
     * 这里按需求拆开了，四种组合：
     *
     * | 颜色 | 尾巴 | 产出 |
     * |---|---|---|
     * | 开 | 开 | 昵称 + 尾巴（结构按用户实测实例：去掉 U+1160，收尾用 U+2066） |
     * | 开 | 关 | 等于上游「尾巴留空」时的产出 |
     * | 关 | 开 | 昵称 + 尾巴；这是把上游结构里的颜色标记去掉，
     *             上游没有这种输出，实际效果得在 QQ 群里实测 |
     * | 关 | 关 | 直接就是昵称，一个不可见字符都不掺 |
     */
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

        // 尾巴部分：结尾连续的标点会被切下来、倒序后拼到最前（上游行为）。
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