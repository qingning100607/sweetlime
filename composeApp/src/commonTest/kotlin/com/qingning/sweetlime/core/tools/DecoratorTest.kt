package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

/** 自定义装饰（前缀 / 字间 / 后缀）的边界：换行、空串、emoji 都不能出错。 */
class DecoratorTest {

    @Test
    fun wrapsAndInserts() {
        assertEquals("[a\u00B7b\u00B7c]", decorate("abc", "[", "\u00B7", "]"))
    }

    @Test
    fun emptyPartsAreHarmless() {
        assertEquals("abc", decorate("abc", "", "", ""))
        assertEquals("a\u22C6b", decorate("ab", "", "\u22C6", ""))
        assertEquals("<abc>", decorate("abc", "<", "", ">"))
    }

    @Test
    fun neverInsertsAtLineBreaks() {
        // 换行前、换行后都不插：多行文本不该变成一堆符号
        assertEquals("a\u00B7b\nc\u00B7d", decorate("ab\ncd", "", "\u00B7", ""))
        assertEquals("\n", decorate("\n", "", "\u00B7", ""))
    }

    @Test
    fun emptyInputStaysEmpty() {
        // 只填了三段装饰，不该凭空产出一串符号
        assertEquals("", decorate("", "[", "\u00B7", "]"))
    }

    @Test
    fun keepsSurrogatePairsTogether() {
        // 🎉 = U+D83C U+DF89，符号必须落在整个字符之后，不能插进两个代理中间
        val out = decorate("\uD83C\uDF89\uD83C\uDF89", "", "\u00B7", "")
        assertEquals("\uD83C\uDF89\u00B7\uD83C\uDF89", out)
    }
}