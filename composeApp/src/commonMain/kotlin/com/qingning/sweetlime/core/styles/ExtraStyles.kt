package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.mapping.SUBSCRIPT_MAP
import com.qingning.sweetlime.core.mapping.SUPERSCRIPT_MAP
import com.qingning.sweetlime.core.mapping.codePointToString
import com.qingning.sweetlime.core.mapping.mapEach
import com.qingning.sweetlime.core.tools.toFlyingBird
import com.qingning.sweetlime.core.transform

/** 通用字符映射样式。 */
private fun mapStyle(id: String, title: String, group: StyleGroup, table: Map<Char, String>): TextTransform =
    transform(id, title, group) { input -> mapEach(input) { table[it] } }

/** 上标 / 下标。 */
internal val superSubStyles: List<TextTransform>
    get() = listOf(
    mapStyle("sup", "上标", StyleGroup.SUPSUB, SUPERSCRIPT_MAP),
    mapStyle("sub", "下标", StyleGroup.SUPSUB, SUBSCRIPT_MAP),
)

/**
 * 彩色字母：源站实际输出的是 Unicode 区域指示符字母（Regional Indicator，U+1F1E6 起）。
 * 它在增补平面，必须用 codePointToString 拼出代理对，否则会变成乱码。
 */
private fun regionalIndicator(ch: Char): String? = when {
    ch in 'A'..'Z' -> codePointToString(0x1F1E6 + (ch - 'A'))
    ch in 'a'..'z' -> codePointToString(0x1F1E6 + (ch - 'a'))
    else -> null
}

internal val colorStyles: List<TextTransform>
    get() = listOf(
    transform("color_ri", "区域指示符", StyleGroup.COLOR) { input ->
        buildString(input.length * 3) {
            input.forEach { ch ->
                val ri = regionalIndicator(ch)
                if (ri != null) append(ri).append(' ') else append(ch)
            }
        }.trimEnd()
    },
)

// 字母网名（源自 jijie.ink 的 CharMap）已按要求下线，数据表一并删除。

/** 飞鸟文：每个字后面追加组合字符 ོ，部分 App 会渲染成小鸟形状。 */
internal val birdStyles: List<TextTransform>
    get() = listOf(
    transform("bird", "飞鸟文", StyleGroup.BIRD) { input -> toFlyingBird(input) },
)

/** 空白字符：利用不可见 / 无宽字符把网名“拉长”或直接隐形。 */
private fun blankAppend(id: String, title: String, mark: String): TextTransform =
    transform(id, title, StyleGroup.BLANK) { input ->
        buildString(input.length * 2) {
            input.forEach { ch ->
                append(ch)
                if (ch != '\n' && ch != '\r') append(mark)
            }
        }
    }

internal val blankStyles: List<TextTransform>
    get() = listOf(
    blankAppend("blank_zwsp", "零宽空格", "\u200B"),       // ZERO WIDTH SPACE
    blankAppend("blank_wj", "词连接符", "\u2060"),         // WORD JOINER
    blankAppend("blank_hangul", "韩文填充", "\u3164"),     // HANGUL FILLER（有宽度但不显字）
    blankAppend("blank_mongolian", "蒙文分隔", "\u180E"),  // MONGOLIAN VOWEL SEPARATOR
    blankAppend("blank_ideographic", "全角空格", "\u3000"), // IDEOGRAPHIC SPACE
    transform("blank_pure", "纯空白", StyleGroup.BLANK) { input ->
        buildString(input.length) {
            input.forEach { ch -> if (ch == '\n' || ch == '\r') append(ch) else append("\u3164") }
        }
    },
)

/** 除英文花体、叠加符号外的所有扩展样式。 */
internal val extendedStyles: List<TextTransform>
    get() =
    superSubStyles + colorStyles +
        symbolStyles + huayangStyles + wingStyles + vineStyles + birdStyles + blankStyles
