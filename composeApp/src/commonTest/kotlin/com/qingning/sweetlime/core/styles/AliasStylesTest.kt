package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.TransformRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AliasStylesTest {

    private fun byId(id: String) = TransformRegistry.byId(id)!!

    @Test
    fun everyStyleIdIsUnique() {
        val ids = TransformRegistry.all.map { it.id }
        val dup = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue(dup.isEmpty(), "样式 id 重复：$dup")
    }

    @Test
    fun reverseOrdersCodePoints() {
        assertEquals("cba", byId("alias_reverse").transform("abc"))

        assertEquals("界世好你", byId("alias_reverse").transform("你好世界"))
    }

    @Test
    fun reverseKeepsSurrogatePairsTogether() {

        val out = byId("alias_reverse").transform("a👦b")
        assertEquals("b👦a", out)
        assertEquals(4, out.length, "长度应为 4 个 UTF-16 单元（b + 代理对 + a）")
        assertTrue(out[1].isHighSurrogate() && out[2].isLowSurrogate(), "代理对应当紧挨着出现")
    }

    @Test
    fun spacingInsertsBetweenCodePointsNotSurrogates() {

        val dot = byId("alias_dot_spacing").transform("a\uD83C\uDF89")
        assertEquals("a\u00B7\uD83C\uDF89", dot)
        assertTrue(dot[2].isHighSurrogate() && dot[3].isLowSurrogate(), "emoji 必须完整")

        val star = byId("alias_star_spacing").transform("a\uD83C\uDF89")
        assertEquals("a\u22C6\uD83C\uDF89", star)
        assertTrue(star[2].isHighSurrogate() && star[3].isLowSurrogate(), "emoji 必须完整")
    }

    @Test
    fun rtlFlipWrapsWithDirectionControls() {
        val out = byId("alias_rtl_flip").transform("abc")
        assertEquals("\u202E" + "abc" + "\u202C", out)
    }

    @Test
    fun fullwidthMapsAsciiOnly() {
        assertEquals("ＡＢＣ１２３！", byId("alias_fullwidth").transform("ABC123!"))

        assertEquals("全角\u3000Ａ中👦", byId("alias_fullwidth").transform("全角 A中👦"))

        assertEquals("ＡＢ", byId("alias_fullwidth").transform("ＡＢ"))
    }

    @Test
    fun halfwidthUndoesFullwidth() {
        val full = byId("alias_fullwidth").transform("SweetLime 2.0 <ok>")
        assertEquals("SweetLime 2.0 <ok>", byId("alias_halfwidth").transform(full))

        assertEquals("a b", byId("alias_halfwidth").transform("a\u3000b"))
    }

    @Test
    fun dotSpacingInsertsBetweenCharsOnly() {
        assertEquals("a\u00B7b\u00B7c", byId("alias_dot_spacing").transform("abc"))

        assertTrue(!byId("alias_dot_spacing").transform("a").contains('\u00B7'))

        assertEquals("a\u00B7b\nc\u00B7d", byId("alias_dot_spacing").transform("ab\ncd"))
    }

    @Test
    fun starSpacingUsesStarMark() {
        assertEquals("星\u22C6间", byId("alias_star_spacing").transform("星间"))
    }

    @Test
    fun brailleMatchesStandardDots() {

        assertEquals("\u2801\u2803\u2809", byId("alias_braille").transform("abc"))

        assertEquals("\u283A", byId("alias_braille").transform("w"))

        assertEquals("\u2801\u2803\u2809", byId("alias_braille").transform("ABC"))
    }

    @Test
    fun brailleValuesStayInBrailleBlock() {
        val out = byId("alias_braille").transform("abcdefghijklmnopqrstuvwxyz")
        assertEquals(26, out.length)
        assertTrue(out.all { it.code in 0x2800..0x28FF }, "有字符跑出 Braille Patterns 区：$out")

        assertEquals(26, out.toSet().size)
    }

    @Test
    fun runeValuesStayInRunicBlock() {
        val out = byId("alias_rune").transform("abcdefghijklmnopqrstuvwxyz")
        assertEquals(26, out.length)
        assertTrue(out.all { it.code in 0x16A0..0x16FF }, "有字符跑出 Runic 区：$out")

        assertEquals(25, out.toSet().size)
    }

    @Test
    fun runeIsCaseInsensitiveAndPassesThroughOthers() {
        val lower = byId("alias_rune").transform("sweet")
        val upper = byId("alias_rune").transform("SWEET")
        assertEquals(lower, upper)

        assertEquals("中，！", byId("alias_rune").transform("中，！"))
    }

    @Test
    fun keycapWrapsDigitsOnly() {
        assertEquals("1\uFE0F\u20E3" + "2\uFE0F\u20E3", byId("alias_keycap").transform("12"))
        assertEquals("v1\uFE0F\u20E3", byId("alias_keycap").transform("v1"))
        assertEquals("abc", byId("alias_keycap").transform("abc"))
    }
}