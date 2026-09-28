package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.mapping.codePointToString
import com.qingning.sweetlime.core.mapping.mapEach
import com.qingning.sweetlime.core.mapping.mathRange
import com.qingning.sweetlime.core.transform

// ---------------------------------------------------------------------------
// 需要补位的字符表（这些码位在数学字母符号区里是空洞，用 Letterlike Symbols 顶）
// ---------------------------------------------------------------------------
private val SCRIPT_EXCEPTIONS = mapOf(
    'B' to "\u212C", 'E' to "\u2130", 'F' to "\u2131", 'H' to "\u210B",
    'I' to "\u2110", 'L' to "\u2112", 'M' to "\u2133", 'R' to "\u211B",
    'e' to "\u212F", 'g' to "\u210A", 'o' to "\u2134",
)

private val FRAKTUR_EXCEPTIONS = mapOf(
    'C' to "\u212D", 'H' to "\u210C", 'I' to "\u2111",
    'R' to "\u211C", 'Z' to "\u2128",
)

private val DOUBLE_STRUCK_EXCEPTIONS = mapOf(
    'C' to "\u2102", 'H' to "\u210D", 'N' to "\u2115", 'P' to "\u2119",
    'Q' to "\u211A", 'R' to "\u211D", 'Z' to "\u2124",
)

private val SMALL_CAPS = mapOf(
    'a' to '\u1D00', 'b' to '\u0299', 'c' to '\u1D04', 'd' to '\u1D05',
    'e' to '\u1D07', 'f' to '\uA730', 'g' to '\u0262', 'h' to '\u029C',
    'i' to '\u026A', 'j' to '\u1D0A', 'k' to '\u1D0B', 'l' to '\u029F',
    'm' to '\u1D0D', 'n' to '\u0274', 'o' to '\u1D0F', 'p' to '\u1D18',
    'q' to '\uA7AF', 'r' to '\u0280', 's' to '\uA731', 't' to '\u1D1B',
    'u' to '\u1D1C', 'v' to '\u1D20', 'w' to '\u1D21', 'x' to 'x',
    'y' to '\u028F', 'z' to '\u1D22',
)

// ---------------------------------------------------------------------------
// 样式定义
// ---------------------------------------------------------------------------
private fun mathStyle(
    id: String,
    title: String,
    upper: Int,
    lower: Int,
    digits: Int? = null,
    exceptions: Map<Char, String> = emptyMap(),
): TextTransform = transform(id, title, StyleGroup.LATIN) { input ->
    mapEach(input, mathRange(upper, lower, digits, exceptions))
}

private fun circledTransform(char: Char): String? = when {
    char in 'A'..'Z' -> codePointToString(0x24B6 + (char - 'A'))
    char in 'a'..'z' -> codePointToString(0x24D0 + (char - 'a'))
    char == '0' -> codePointToString(0x24EA)
    char in '1'..'9' -> codePointToString(0x2460 + (char - '1'))
    else -> null
}

/** 粗圆圈：Unicode 只有大写的「负片圆环字母」（U+1F150 起），小写也映射成同一批字形。 */
private fun boldCircledTransform(char: Char): String? {
    val upper = when {
        char in 'A'..'Z' -> char
        char in 'a'..'z' -> char - 32
        else -> return null
    }
    return codePointToString(0x1F150 + (upper - 'A'))
}

/**
 * 倒置字：整体翻转 180°。
 * 做法是先逐行把字符顺序倒过来，再把每个字符替换成它的「倒过来看」字形。
 */
private val FLIP_MAP: Map<Char, String> = mapOf(
    'a' to "\u0250", 'b' to "q", 'c' to "\u0254", 'd' to "p", 'e' to "\u01DD",
    'f' to "\u025F", 'g' to "\u0183", 'h' to "\u0265", 'i' to "\u1D09", 'j' to "\u027E",
    'k' to "\u029E", 'l' to "l", 'm' to "\u026F", 'n' to "u", 'o' to "o",
    'p' to "d", 'q' to "b", 'r' to "\u0279", 's' to "s", 't' to "\u0287",
    'u' to "n", 'v' to "\u028C", 'w' to "\u028D", 'x' to "x", 'y' to "\u028E",
    'z' to "z",
    'A' to "\u2200", 'B' to "\uD801\uDC02", 'C' to "\u0186", 'D' to "\u25D6", 'E' to "\u018E",
    'F' to "\u2132", 'G' to "\u2141", 'H' to "H", 'I' to "I", 'J' to "\u017F",
    'K' to "\u029E", 'L' to "\u02E5", 'M' to "W", 'N' to "N", 'O' to "O",
    'P' to "\u0500", 'Q' to "\u038C", 'R' to "\u1D1A", 'S' to "S", 'T' to "\u22A5",
    'U' to "\u2229", 'V' to "\u039B", 'W' to "M", 'X' to "X", 'Y' to "\u2144",
    'Z' to "Z",
    '0' to "0", '1' to "\u0196", '2' to "\u1220", '3' to "\u0190", '4' to "\u3123",
    '5' to "\u03DB", '6' to "9", '7' to "\u3125", '8' to "8", '9' to "6",
    '.' to "\u02D9", ',' to "'", '?' to "\u00BF", '!' to "\u00A1",
    '"' to "\u201E", '\'' to ",", '(' to ")", ')' to "(", '[' to "]", ']' to "[",
    '{' to "}", '}' to "{", '<' to ">", '>' to "<", '&' to "\u214B", '_' to "\u203E",
    ';' to "\u061B", '\u3001' to ",",
)

/** 按码位（不是按 Char）倒序，避免把增补平面的代理对拆坏。 */
private fun reverseCodePoints(text: String): String {
    val units = ArrayList<String>(text.length)
    var i = 0
    while (i < text.length) {
        val c = text[i]
        val isPair = c in '\uD800'..'\uDBFF' &&
            i + 1 < text.length && text[i + 1] in '\uDC00'..'\uDFFF'
        if (isPair) {
            units.add(text.substring(i, i + 2))
            i += 2
        } else {
            units.add(c.toString())
            i += 1
        }
    }
    return buildString(text.length) {
        for (j in units.indices.reversed()) append(units[j])
    }
}

internal val latinStyles: List<TextTransform> = listOf(
    // Mathematical Bold
    mathStyle("latin_bold", "粗体", 0x1D400, 0x1D41A, 0x1D7CE),
    // Mathematical Italic（小写 h 是空洞，用 ℎ U+210E）
    mathStyle(
        "latin_italic", "斜体", 0x1D434, 0x1D44E,
        exceptions = mapOf('h' to "\u210E"),
    ),
    mathStyle("latin_bold_italic", "粗斜体", 0x1D468, 0x1D482, 0x1D7CE),
    mathStyle(
        "latin_script", "手写体", 0x1D49C, 0x1D4B6,
        exceptions = SCRIPT_EXCEPTIONS,
    ),
    mathStyle("latin_bold_script", "粗手写体", 0x1D4D0, 0x1D4EA),
    mathStyle(
        "latin_fraktur", "哥特体", 0x1D504, 0x1D51E,
        exceptions = FRAKTUR_EXCEPTIONS,
    ),
    mathStyle(
        "latin_double_struck", "双线体", 0x1D538, 0x1D552, 0x1D7D8,
        exceptions = DOUBLE_STRUCK_EXCEPTIONS,
    ),
    transform("latin_circled", "圆圈体", StyleGroup.LATIN) { input ->
        mapEach(input) { circledTransform(it) }
    },
    transform("latin_small_caps", "小型大写", StyleGroup.LATIN) { input ->
        mapEach(input) { SMALL_CAPS[it]?.toString() }
    },
    mathStyle("latin_monospace", "等宽体", 0x1D670, 0x1D68A, 0x1D7F6),
    transform("latin_bold_circled", "粗圆圈", StyleGroup.LATIN) { input ->
        mapEach(input) { boldCircledTransform(it) }
    },
    transform("latin_upside_down", "倒置字", StyleGroup.LATIN) { input ->
        input.split('\n').joinToString("\n") { line ->
            val reversed = reverseCodePoints(line)
            buildString(reversed.length) {
                reversed.forEach { ch -> append(FLIP_MAP[ch] ?: ch.toString()) }
            }
        }
    },
)
