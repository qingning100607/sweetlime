package com.qingning.sweetlime.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/** 代码高亮的配色（深色 / 浅色各一套）。 */
class CodePalette(
    val keyword: Color,
    val string: Color,
    val comment: Color,
    val number: Color,
    val function: Color,
) {
    companion object {
        /** 深色背景用（One Dark 那一挂）。 */
        val Dark = CodePalette(
            keyword = Color(0xFFC678DD),
            string = Color(0xFF98C379),
            comment = Color(0xFF7F848E),
            number = Color(0xFFD19A66),
            function = Color(0xFF61AFEF),
        )

        /** 浅色背景用（GitHub Light 那一挂）。 */
        val Light = CodePalette(
            keyword = Color(0xFFA626A4),
            string = Color(0xFF50A14F),
            comment = Color(0xFFA0A1A7),
            number = Color(0xFF986801),
            function = Color(0xFF4078F2),
        )
    }
}

/**
 * 极简语法高亮。
 *
 * 不做完整的词法分析，只用**一遍扫描**按「行注释 / 块注释 / 字符串 / 数字 / 标识符」
 * 分段上色 —— 好处是顺序天然正确：`"http://a"` 里的 `//` 不会被当成注释，
 * 字符串里的关键词也不会被染色。
 *
 * 它不区分语言（关键词表覆盖了常见几门），认不出来的就当普通文本：
 * 高亮只影响观感，永远不会改动文本本身。
 */
object CodeHighlighter {

    private val KEYWORDS = setOf(
        // Kotlin / Java / C 系
        "fun", "val", "var", "if", "else", "when", "for", "while", "do", "return",
        "break", "continue", "class", "interface", "object", "enum", "data", "sealed",
        "override", "private", "public", "protected", "internal", "abstract", "open",
        "const", "import", "package", "try", "catch", "finally", "throw", "new",
        "this", "super", "is", "as", "in", "by", "suspend", "typealias", "companion",
        "init", "lateinit", "void", "static", "final", "extends", "implements",
        "instanceof", "typeof", "function", "let", "export", "default", "null",
        "int", "long", "short", "byte", "float", "double", "char", "bool", "boolean",
        "string", "unsigned", "struct", "union", "typedef", "sizeof", "auto",
        // Python / Shell
        "def", "elif", "pass", "from", "with", "global", "nonlocal", "assert", "del",
        "not", "and", "or", "None", "True", "False", "self", "lambda", "yield",
        "async", "await", "print", "then", "fi", "done", "esac", "echo",
        // Rust / Go / 其它
        "fn", "impl", "trait", "pub", "mut", "use", "mod", "match", "loop", "where",
        "crate", "unsafe", "ref", "move", "defer", "chan", "go", "range", "nil",
    )

    fun highlight(source: String, palette: CodePalette): AnnotatedString = buildAnnotatedString {
        append(source)
        var i = 0
        val n = source.length
        while (i < n) {
            val c = source[i]
            when {
                // 行注释：//
                c == '/' && i + 1 < n && source[i + 1] == '/' -> {
                    val end = source.indexOf('\n', i).let { if (it < 0) n else it }
                    addStyle(SpanStyle(color = palette.comment), i, end)
                    i = end
                }
                // 块注释：/* ... */
                c == '/' && i + 1 < n && source[i + 1] == '*' -> {
                    val close = source.indexOf("*/", startIndex = i + 2)
                    val end = if (close < 0) n else close + 2
                    addStyle(SpanStyle(color = palette.comment), i, end)
                    i = end
                }
                // 井号注释（Python / Shell / YAML）：必须是本行第一个非空白字符
                c == '#' && isFirstOnLine(source, i) -> {
                    val end = source.indexOf('\n', i).let { if (it < 0) n else it }
                    addStyle(SpanStyle(color = palette.comment), i, end)
                    i = end
                }
                // 字符串：双引号 / 单引号 / 反引号
                c == '"' || c == '\'' || c == '`' -> {
                    val end = scanString(source, i)
                    addStyle(SpanStyle(color = palette.string), i, end)
                    i = end
                }
                // 数字（只吃「数字.数字」，不会把 1..2 这种区间粘成一块）
                c.isDigit() -> {
                    var j = i
                    while (j < n && source[j].isDigit()) j++
                    if (j < n && source[j] == '.' && j + 1 < n && source[j + 1].isDigit()) {
                        j++
                        while (j < n && source[j].isDigit()) j++
                    }
                    addStyle(SpanStyle(color = palette.number), i, j)
                    i = j
                }
                // 标识符：关键词，或者后面跟着括号的函数名
                c.isLetter() || c == '_' -> {
                    var j = i
                    while (j < n && (source[j].isLetterOrDigit() || source[j] == '_')) j++
                    val word = source.substring(i, j)
                    val style = when {
                        word in KEYWORDS -> SpanStyle(color = palette.keyword)
                        isFollowedByCall(source, j) -> SpanStyle(color = palette.function)
                        else -> null
                    }
                    if (style != null) addStyle(style, i, j)
                    i = j
                }
                else -> i++
            }
        }
    }

    /** 从 [start]（引号位置）扫到配对的引号，返回这段的结束下标。 */
    private fun scanString(source: String, start: Int): Int {
        val quote = source[start]
        var i = start + 1
        while (i < source.length) {
            when {
                source[i] == '\\' -> i += 2
                source[i] == quote -> return i + 1
                // 反引号允许跨行，其余引号遇到换行就当没闭合
                source[i] == '\n' && quote != '`' -> return i
                else -> i++
            }
        }
        return source.length
    }

    /** 标识符后面（跳过空格）是不是 `(` —— 是的话按函数名上色。 */
    private fun isFollowedByCall(source: String, from: Int): Boolean {
        var i = from
        while (i < source.length && source[i] == ' ') i++
        return i < source.length && source[i] == '('
    }

    /** [index] 前面是不是只有空白（直到行首）。 */
    private fun isFirstOnLine(source: String, index: Int): Boolean {
        var i = index - 1
        while (i >= 0 && source[i] != '\n') {
            if (!source[i].isWhitespace()) return false
            i--
        }
        return true
    }
}