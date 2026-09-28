package com.qingning.sweetlime.ui.screen

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 测编辑器的输入期加工：补括号、跳过收尾符、换行缩进、查找范围。
 *
 * 这些都是纯函数，跟界面无关，所以放主源码集外的测试里跑。
 */
class CodeEditToolsTest {

    /** 模拟「在 [caret] 处插入 [ch]」这一次输入。 */
    private fun typed(before: String, caret: Int, ch: String): TextFieldValue {
        val old = TextFieldValue(before, TextRange(caret))
        val raw = TextFieldValue(
            text = before.substring(0, caret) + ch + before.substring(caret),
            selection = TextRange(caret + ch.length),
        )
        return autoEdit(old, raw)
    }

    @Test
    fun autoClosesBrackets() {
        val result = typed("foo", 3, "(")
        assertEquals("foo()", result.text)
        assertEquals(4, result.selection.start)
    }

    @Test
    fun skipsTheCloserAlreadyThere() {
        val result = typed("foo()", 4, ")")
        assertEquals("foo()", result.text)
        assertEquals(5, result.selection.start)
    }

    @Test
    fun doesNotPairQuoteAfterWord() {
        // don't 的撇号不该被自动配对。
        val result = typed("don", 3, "'")
        assertEquals("don'", result.text)
    }

    @Test
    fun pairsQuoteOnEmptySpot() {
        val result = typed("", 0, "\"")
        assertEquals("\"\"", result.text)
        assertEquals(1, result.selection.start)
    }

    @Test
    fun keepsIndentOnNewline() {
        val result = typed("  a\n  b", 7, "\n")
        assertEquals("  a\n  b\n  ", result.text)
        // 光标落在新行的缩进后面（行首 2 格 + 新行符）。
        assertEquals(10, result.selection.start)
    }

    @Test
    fun goesDeeperAfterOpenBrace() {
        val result = typed("  a {}", 5, "\n")
        assertEquals("  a {\n   \n  }", result.text)
    }

    @Test
    fun leavesPasteAlone() {
        val old = TextFieldValue("abc", TextRange(3))
        val pasted = TextFieldValue("abcdef", TextRange(6))
        assertEquals(pasted, autoEdit(old, pasted))
    }

    @Test
    fun leavesSelectionReplaceAlone() {
        val old = TextFieldValue("abcdef", TextRange(1, 4))
        val replaced = TextFieldValue("axyzef", TextRange(4))
        assertEquals(replaced, autoEdit(old, replaced))
    }

    @Test
    fun countsNonOverlappingMatches() {
        assertEquals(0, countOccurrences("abc", ""))
        assertEquals(0, countOccurrences("abc", "z"))
        assertEquals(1, countOccurrences("aaa", "aa"))
        assertEquals(2, countOccurrences("a-a-a", "a-"))
    }

    @Test
    fun wrapsAroundWhenSearching() {
        assertEquals(2, indexOfFrom("abcabc", "c", 0))
        // 从后面找不到就回到开头。
        assertEquals(0, indexOfFrom("abcabc", "a", 4))
        assertEquals(-1, indexOfFrom("abcabc", "z", 0))
    }
}