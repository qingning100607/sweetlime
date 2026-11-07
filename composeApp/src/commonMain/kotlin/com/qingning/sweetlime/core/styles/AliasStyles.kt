package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform
import com.qingning.sweetlime.core.forEachCodePoint

private val reverseStyle: TextTransform =
    nativeStyle("alias_reverse", "顺序反转", StyleGroup.LATIN) { input ->
        buildString(input.length) {
            var i = input.length - 1
            while (i >= 0) {
                val low = input[i]
                if (low.isLowSurrogate() && i > 0 && input[i - 1].isHighSurrogate()) {

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

private val rtlFlipStyle: TextTransform =
    nativeStyle("alias_rtl_flip", "反写", StyleGroup.LATIN) { input ->
        "\u202E" + input + "\u202C"
    }

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

                val mapped = BRAILLE_MAP[ch.lowercaseChar()]
                if (mapped != null) append(mapped) else append(ch)
            }
        }
    }

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
