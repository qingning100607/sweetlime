package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.mapping.SUBSCRIPT_MAP
import com.qingning.sweetlime.core.mapping.SUPERSCRIPT_MAP
import com.qingning.sweetlime.core.mapping.codePointToString
import com.qingning.sweetlime.core.mapping.mapEach
import com.qingning.sweetlime.core.tools.toFlyingBird
import com.qingning.sweetlime.core.transform
import com.qingning.sweetlime.core.forEachCodePoint

private fun mapStyle(id: String, title: String, group: StyleGroup, table: Map<Char, String>): TextTransform =
    transform(id, title, group) { input -> mapEach(input) { table[it] } }

internal val superSubStyles: List<TextTransform>
    get() = listOf(
    mapStyle("sup", "上标", StyleGroup.SUPSUB, SUPERSCRIPT_MAP),
    mapStyle("sub", "下标", StyleGroup.SUPSUB, SUBSCRIPT_MAP),
)

private fun regionalIndicator(ch: Char): String? = when {
    ch in 'A'..'Z' -> codePointToString(0x1F1E6 + (ch - 'A'))
    ch in 'a'..'z' -> codePointToString(0x1F1E6 + (ch - 'a'))
    else -> null
}

internal val colorStyles: List<TextTransform>
    get() = listOf(
    transform("color_ri", "彩色字母", StyleGroup.COLOR) { input ->
        buildString(input.length * 3) {
            input.forEach { ch ->
                val ri = regionalIndicator(ch)
                if (ri != null) append(ri).append(' ') else append(ch)
            }
        }.trimEnd()
    },
)

internal val birdStyles: List<TextTransform>
    get() = listOf(
    transform("bird", "飞鸟文", StyleGroup.HUAYANG) { input -> toFlyingBird(input) },
)

private fun blankAppend(id: String, title: String, mark: String): TextTransform =
    transform(id, title, StyleGroup.BLANK) { input ->
        buildString(input.length * 2) {

            input.forEachCodePoint { ch ->
                append(ch)
                if (ch != "\n" && ch != "\r") append(mark)
            }
        }
    }

internal val blankStyles: List<TextTransform>
    get() = listOf(
    blankAppend("blank_zwsp", "零宽空格", "\u200B"),
    blankAppend("blank_wj", "词连接符", "\u2060"),
    blankAppend("blank_hangul", "韩文填充", "\u3164"),
    blankAppend("blank_mongolian", "蒙文分隔", "\u180E"),
    blankAppend("blank_ideographic", "全角空格", "\u3000"),
    transform("blank_pure", "纯空白", StyleGroup.BLANK) { input ->
        buildString(input.length) {
            input.forEach { ch -> if (ch == '\n' || ch == '\r') append(ch) else append("\u3164") }
        }
    },
)

internal val extendedStyles: List<TextTransform>
    get() =
    superSubStyles + colorStyles +
        symbolStyles + huayangStyles + wingStyles + vineStyles + birdStyles + blankStyles
