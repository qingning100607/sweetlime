package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.TransformRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 「字符置换 / 排版」类样式的回归。
 *
 * 这批样式的坑在于：**看不太出来对错**（盲文、符文长什么样没几个人能背），
 * 所以这里钉住的都是可以机械核对的硬性质：
 * 码点落在哪个 Unicode 区、代理对会不会被劈开、全角半角能不能来回。
 */
class AliasStylesTest {

    private fun byId(id: String) = TransformRegistry.byId(id)!!

    @Test
    fun everyStyleIdIsUnique() {
        val ids = TransformRegistry.all.map { it.id }
        val dup = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue(dup.isEmpty(), "样式 id 重复：$dup")
    }

    /* ---------------- 顺序反转 ---------------- */

    @Test
    fun reverseOrdersCodePoints() {
        assertEquals("cba", byId("alias_reverse").transform("abc"))
        // 中文也一样翻
        assertEquals("界世好你", byId("alias_reverse").transform("你好世界"))
    }

    @Test
    fun reverseKeepsSurrogatePairsTogether() {
        // 👦 是增补平面字符（代理对）。翻转后它必须还是一个完整字符，
        // 不能被拆成两个孤立代理（那样会渲染成 □□）。
        val out = byId("alias_reverse").transform("a👦b")
        assertEquals("b👦a", out)
        assertEquals(4, out.length, "长度应为 4 个 UTF-16 单元（b + 代理对 + a）")
        assertTrue(out[1].isHighSurrogate() && out[2].isLowSurrogate(), "代理对应当紧挨着出现")
    }

    /* ---------------- 插入型：分隔符必须落在码点之间 ---------------- */
    @Test
    fun spacingInsertsBetweenCodePointsNotSurrogates() {
        // 🎉 = 代理对。分隔符要插在整个字符**之后**，不能插进两个代理中间。
        val dot = byId("alias_dot_spacing").transform("a\uD83C\uDF89")
        assertEquals("a\u00B7\uD83C\uDF89", dot)
        assertTrue(dot[2].isHighSurrogate() && dot[3].isLowSurrogate(), "emoji 必须完整")

        val star = byId("alias_star_spacing").transform("a\uD83C\uDF89")
        assertEquals("a\u22C6\uD83C\uDF89", star)
        assertTrue(star[2].isHighSurrogate() && star[3].isLowSurrogate(), "emoji 必须完整")
    }

    /* ---------------- 反写（方向翻转） ---------------- */

    @Test
    fun rtlFlipWrapsWithDirectionControls() {
        val out = byId("alias_rtl_flip").transform("abc")
        assertEquals("\u202E" + "abc" + "\u202C", out)
    }

    /* ---------------- 全角 / 半角 ---------------- */

    @Test
    fun fullwidthMapsAsciiOnly() {
        assertEquals("ＡＢＣ１２３！", byId("alias_fullwidth").transform("ABC123!"))
        // 空格变成全角空格；中文、emoji 原样（中文本来就没有半角写法）
        assertEquals("全角\u3000Ａ中👦", byId("alias_fullwidth").transform("全角 A中👦"))
        // 已是全角的字符再转一次不变（幂等）
        assertEquals("ＡＢ", byId("alias_fullwidth").transform("ＡＢ"))
    }

    @Test
    fun halfwidthUndoesFullwidth() {
        val full = byId("alias_fullwidth").transform("SweetLime 2.0 <ok>")
        assertEquals("SweetLime 2.0 <ok>", byId("alias_halfwidth").transform(full))
        // 全角空格也要能还原成普通空格
        assertEquals("a b", byId("alias_halfwidth").transform("a\u3000b"))
    }

    /* ---------------- 间隔 ---------------- */

    @Test
    fun dotSpacingInsertsBetweenCharsOnly() {
        assertEquals("a\u00B7b\u00B7c", byId("alias_dot_spacing").transform("abc"))
        // 首尾不加间隔符
        assertTrue(!byId("alias_dot_spacing").transform("a").contains('\u00B7'))
        // 换行原样，且在换行后重新开始（不会出现行首间隔符）
        assertEquals("a\u00B7b\nc\u00B7d", byId("alias_dot_spacing").transform("ab\ncd"))
    }

    @Test
    fun starSpacingUsesStarMark() {
        assertEquals("星\u22C6间", byId("alias_star_spacing").transform("星间"))
    }

    /* ---------------- 盲文 ---------------- */

    @Test
    fun brailleMatchesStandardDots() {
        // 英文盲文标准点阵：a=1 点(U+2801)、b=12 点(U+2803)、c=14 点(U+2809)
        assertEquals("\u2801\u2803\u2809", byId("alias_braille").transform("abc"))
        // w 是唯一带第 6 点的字母（2456 点 = U+283A）
        assertEquals("\u283A", byId("alias_braille").transform("w"))
        // 大写按小写映射（盲文没有大小写）
        assertEquals("\u2801\u2803\u2809", byId("alias_braille").transform("ABC"))
    }

    @Test
    fun brailleValuesStayInBrailleBlock() {
        val out = byId("alias_braille").transform("abcdefghijklmnopqrstuvwxyz")
        assertEquals(26, out.length)
        assertTrue(out.all { it.code in 0x2800..0x28FF }, "有字符跑出 Braille Patterns 区：$out")
        // 26 个字母应当给出 26 个互不相同的盲文字符
        assertEquals(26, out.toSet().size)
    }

    /* ---------------- 卢恩符文 ---------------- */

    @Test
    fun runeValuesStayInRunicBlock() {
        val out = byId("alias_rune").transform("abcdefghijklmnopqrstuvwxyz")
        assertEquals(26, out.length)
        assertTrue(out.all { it.code in 0x16A0..0x16FF }, "有字符跑出 Runic 区：$out")
        // c 与 k 共用同一个符文（ᚲ），所以去重后是 25 个
        assertEquals(25, out.toSet().size)
    }

    @Test
    fun runeIsCaseInsensitiveAndPassesThroughOthers() {
        val lower = byId("alias_rune").transform("sweet")
        val upper = byId("alias_rune").transform("SWEET")
        assertEquals(lower, upper)
        // 中文与标点原样
        assertEquals("中，！", byId("alias_rune").transform("中，！"))
    }

    /* ---------------- 键帽数字 ---------------- */

    @Test
    fun keycapWrapsDigitsOnly() {
        assertEquals("1\uFE0F\u20E3" + "2\uFE0F\u20E3", byId("alias_keycap").transform("12"))
        assertEquals("v1\uFE0F\u20E3", byId("alias_keycap").transform("v1"))
        assertEquals("abc", byId("alias_keycap").transform("abc"))
    }
}