package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.TransformRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 无衬线家族 + 粗哥特体的回归。
 *
 * 这几个家族以前**整个漏掉了**（只有粗体/斜体/手写/哥特/双线/等宽），
 * 所以这里钉住三件事：字面量对不对、数字该有则有该无则无、代理对没被劈开。
 */
class SansSerifStylesTest {
    private fun byId(id: String) = TransformRegistry.byId(id)!!

    /** 把一个「单码点字符串」解成 Int（不用 java.lang.Character，common 里也能用）。 */
    private fun codePointOf(s: String): Int =
        if (s.length == 1) {
            s[0].code
        } else {
            ((s[0].code - 0xD800) shl 10) + (s[1].code - 0xDC00) + 0x10000
        }

    /** 码点数：测试自己数，不依赖被测实现。 */
    private fun cpLen(s: String): Int {
        var n = 0
        var i = 0
        while (i < s.length) {
            i += if (s[i].isHighSurrogate() && i + 1 < s.length && s[i + 1].isLowSurrogate()) 2 else 1
            n++
        }
        return n
    }

    @Test
    fun sansSerifBoldMatchesExpectedGlyphs() {
        val out = byId("latin_sans_serif_bold").transform("SweetLime")
        assertEquals("𝗦𝘄𝗲𝗲𝘁𝗟𝗶𝗺𝗲", out)
        // 9 个码点：若按 char 数会变成 18
        assertEquals(9, cpLen(out))
    }

    @Test
    fun sansSerifMapsLettersAndDigits() {
        assertEquals("𝖠𝖻𝖼 𝟢𝟣𝟫", byId("latin_sans_serif").transform("Abc 019"))
    }

    @Test
    fun boldFrakturMatchesExpectedGlyphs() {
        assertEquals("𝕾𝖜𝖊𝖊𝖙", byId("latin_bold_fraktur").transform("Sweet"))
    }

    @Test
    fun italicFamiliesLeaveDigitsUntouched() {
        // Unicode 没给无衬线斜体配数字：数字必须原样保留，不能变成方块或吞掉
        for (id in listOf("latin_sans_serif_italic", "latin_sans_serif_bold_italic")) {
            assertEquals("0", byId(id).transform("0"), id)
            assertEquals("123", byId(id).transform("123"), id)
        }
    }

    @Test
    fun everyFamilyCoversAll52LettersWithoutSplitting() {
        val families = listOf(
            "latin_bold_fraktur" to (0x1D56C to 0x1D586),
            "latin_sans_serif" to (0x1D5A0 to 0x1D5BA),
            "latin_sans_serif_bold" to (0x1D5D4 to 0x1D5EE),
            "latin_sans_serif_italic" to (0x1D608 to 0x1D622),
            "latin_sans_serif_bold_italic" to (0x1D63C to 0x1D656),
        )
        for ((id, bases) in families) {
            val style = byId(id)
            for (i in 0 until 26) {
                val up = style.transform(('A' + i).toString())
                val low = style.transform(('a' + i).toString())
                assertEquals(1, cpLen(up), "$id A+$i 不是一个完整码点")
                assertEquals(1, cpLen(low), "$id a+$i 不是一个完整码点")
                assertEquals(bases.first + i, codePointOf(up), "$id 大写 $i 码位不对")
                assertEquals(bases.second + i, codePointOf(low), "$id 小写 $i 码位不对")
                assertTrue(up != ('A' + i).toString(), "$id 没映射大写 $i")
                assertTrue(low != ('a' + i).toString(), "$id 没映射小写 $i")
            }
        }
    }
}
