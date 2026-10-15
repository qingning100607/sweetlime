package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform
import com.qingning.sweetlime.core.forEachCodePoint

/*
 * 「字符置换 / 排版」类样式：文字还是那些字，但一个字符换成一个字符，
 * 或者在字与字之间插符 —— 和 TemplateStyles 的「前后加图案」不是一回事。
 *
 * 不重复已有：
 * - 倒置字（latin_upside_down）已经有了；
 * - 「彩色字母」（color_ri）就是常说的 Emoji / 国旗方块字母，也有了。
 *
 * 表里的字符全部用 \uXXXX 转义写，源码保持纯 ASCII：
 * - 盲文用英文字母标准点阵算出来的（Braille Patterns U+2800..U+28FF）；
 * - 符文用老弗萨克（Elder Futhark，U+16A0..U+16FF）里「一个字母一个字面」的常见对齐。
 */

/** 顺序反转：整串从右往左翻过来，按**码点**翻，不把 emoji / 增补平面字符劈成半个。 */
private val reverseStyle: TextTransform =
    nativeStyle("alias_reverse", "顺序反转", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            var i = input.length - 1
            while (i >= 0) {
                val low = input[i]
                if (low.isLowSurrogate() && i > 0 && input[i - 1].isHighSurrogate()) {
                    // 代理对：连着高代理一起搬，别拆散。
                    append(input[i - 1])
                    append(low)
                    i -= 2
                } else {
                    append(low)
                    i--
                }
            }
        }
    }

/**
 * 反写：用 RTL 覆盖符（U+202E）让渲染器把整串**从右往左**排，末尾用 U+202C 复位。
 *
 * 和「顺序反转」的区别：这个是靠方向控制字符，字符顺序没变，
 * 所以在可以正常处理双向文本的地方（QQ / 微信昵称）看起来是反的，
 * 复制出去也是反的 —— 但一旦某处把控制符过滤掉，它就会"变回正的"。
 */
private val rtlFlipStyle: TextTransform =
    nativeStyle("alias_rtl_flip", "反写", StyleGroup.LATIN) { input ->
        "\u202E" + input + "\u202C"
    }

/**
 * 全角：ASCII 可见字符（! 到 ~）平移到全角区（+0xFEE0），空格换成全角空格（U+3000）。
 * 中文、emoji 一律原样保留。
 */
private val fullwidthStyle: TextTransform =
    nativeStyle("alias_fullwidth", "全角", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            input.forEach { ch ->
                when (ch) {
                    ' ' -> append('\u3000')
                    in '!'..'~' -> append((ch.code + 0xFEE0).toChar())
                    else -> append(ch)
                }
            }
        }
    }

/** 半角：全角区（U+FF01..U+FF5E）平移回 ASCII，全角空格换成空格。 */
private val halfwidthStyle: TextTransform =
    nativeStyle("alias_halfwidth", "半角", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            input.forEach { ch ->
                when (ch) {
                    '\u3000' -> append(' ')
                    in '\uFF01'..'\uFF5E' -> append((ch.code - 0xFEE0).toChar())
                    else -> append(ch)
                }
            }
        }
    }

/**
 * 点间隔：字与字之间插入「间隔符 · (U+00B7)」，首尾不加，换行原样保留。
 * 中英文都生效（按**码点**处理，emoji 不会被分隔符劈开）。
 */
private val dotSpacingStyle: TextTransform =
    nativeStyle("alias_dot_spacing", "点间隔", StyleGroup.LATIN) { input ->
        buildString(input.length * 2) {
            var first = true
            input.forEachCodePoint { ch ->
                if (ch == "\n" || ch == "\r") {
                    append(ch)
                    first = true
                } else {
                    if (!first) append("\u00B7")
                    append(ch)
                    first = false
                }
            }
        }
    }

/**
 * 星间隔：字与字之间插入「间隔符 ⋆ (U+22C6)」，首尾不加，换行原样保留。
 * 中英文都生效（按**码点**处理，emoji 不会被分隔符劈开）。
 */
private val starSpacingStyle: TextTransform =
    nativeStyle("alias_star_spacing", "星间隔", StyleGroup.LATIN) { input ->
        buildString(input.length * 2) {
            var first = true
            input.forEachCodePoint { ch ->
                if (ch == "\n" || ch == "\r") {
                    append(ch)
                    first = true
                } else {
                    if (!first) append("\u22C6")
                    append(ch)
                    first = false
                }
            }
        }
    }

/**
 * 盲文：英文字母按**标准英文盲文点阵**映射到 Braille Patterns（U+2800 区）。
 * 点阵是算出来的（a=1 点、b=12 点、w=2456 点…），不是手抄的字符表。
 */
private val BRAILLE_MAP: Map<Char, String> = mapOf(
    '\u0061' to "\u2801",
    '\u0062' to "\u2803",
    '\u0063' to "\u2809",
    '\u0064' to "\u2819",
    '\u0065' to "\u2811",
    '\u0066' to "\u280B",
    '\u0067' to "\u281B",
    '\u0068' to "\u2813",
    '\u0069' to "\u280A",
    '\u006A' to "\u281A",
    '\u006B' to "\u2805",
    '\u006C' to "\u2807",
    '\u006D' to "\u280D",
    '\u006E' to "\u281D",
    '\u006F' to "\u2815",
    '\u0070' to "\u280F",
    '\u0071' to "\u281F",
    '\u0072' to "\u2817",
    '\u0073' to "\u280E",
    '\u0074' to "\u281E",
    '\u0075' to "\u2825",
    '\u0076' to "\u2827",
    '\u0077' to "\u283A",
    '\u0078' to "\u282D",
    '\u0079' to "\u283D",
    '\u007A' to "\u2835",
)

private val brailleStyle: TextTransform =
    nativeStyle("alias_braille", "盲文", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            input.forEach { ch ->
                // 盲文没有大小写，大写字母按小写映射（AB → 两个盲文字母）。
                val mapped = BRAILLE_MAP[ch.lowercaseChar()]
                if (mapped != null) append(mapped) else append(ch)
            }
        }
    }

/**
 * 卢恩符文：a-z 映射到老弗萨克符文（Elder Futhark，全部在 U+16A0..U+16FF 区内，
 * 测试会逐个核对字符确实属于 Runic 区）。符文没有大小写，大写字母按小写映射。
 *
 * 说明：这是「一个拉丁字母对一个字面」的**显示对齐**，不是语言学的音译规则。
 */
private val RUNE_MAP: Map<Char, String> = mapOf(
    '\u0061' to "\u16A8",
    '\u0062' to "\u16D2",
    '\u0063' to "\u16B2",
    '\u0064' to "\u16DE",
    '\u0065' to "\u16D6",
    '\u0066' to "\u16A0",
    '\u0067' to "\u16B7",
    '\u0068' to "\u16BA",
    '\u0069' to "\u16C1",
    '\u006A' to "\u16C3",
    '\u006B' to "\u16B2",
    '\u006C' to "\u16DA",
    '\u006D' to "\u16D7",
    '\u006E' to "\u16BE",
    '\u006F' to "\u16DF",
    '\u0070' to "\u16C8",
    '\u0071' to "\u16E9",
    '\u0072' to "\u16B1",
    '\u0073' to "\u16CA",
    '\u0074' to "\u16CF",
    '\u0075' to "\u16A2",
    '\u0076' to "\u16A1",
    '\u0077' to "\u16B9",
    '\u0078' to "\u16EA",
    '\u0079' to "\u16E6",
    '\u007A' to "\u16CE",
)

private val runeStyle: TextTransform =
    nativeStyle("alias_rune", "卢恩符文", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            input.forEach { ch ->
                val mapped = RUNE_MAP[ch.lowercaseChar()]
                if (mapped != null) append(mapped) else append(ch)
            }
        }
    }

/** 键帽数字：0-9 换成「键帽 emoji」（数字 + U+FE0F + U+20E3），字母与其它字符原样。 */
private val keycapStyle: TextTransform =
    nativeStyle("alias_keycap", "键帽数字", StyleGroup.LATIN) { input ->
        buildString(input.length * 3) {
            input.forEach { ch ->
                if (ch in '0'..'9') {
                    append(ch)
                    append('\uFE0F')
                    append('\u20E3')
                } else {
                    append(ch)
                }
            }
        }
    }

/** 字符置换 / 排版类的全部样式。 */
internal val aliasStyles: List<TextTransform>
    get() = listOf(
        reverseStyle,
        rtlFlipStyle,
        fullwidthStyle,
        halfwidthStyle,
        dotSpacingStyle,
        starSpacingStyle,
        brailleStyle,
        runeStyle,
        keycapStyle,
    )
