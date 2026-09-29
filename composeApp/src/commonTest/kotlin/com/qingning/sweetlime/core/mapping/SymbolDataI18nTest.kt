package com.qingning.sweetlime.core.mapping

import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.AppLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 「特殊符号」工具里 44 个分类名的多语言回归。
 *
 * 这批分类名曾经一个都没翻译：它们是 SymbolCategory 的构造参数，
 * 而 [SYMBOL_CATEGORIES] 是顶层 val（只构造一次），
 * 所以名字被定死成简体，切到英文后整页分类还是中文。
 * 现在标题改成「读时才翻译」，这里盯住别回退。
 */
class SymbolDataI18nTest {

    private fun hasHanzi(text: String): Boolean = text.any { it in '\u4e00'..'\u9fff' }

    @Test
    fun categoryTitlesFollowLanguage() {
        AppLocale.apply(AppLanguage.ZH)
        val zh = SYMBOL_CATEGORIES.map { it.title }
        assertEquals(44, zh.size, "分类数量变了，词表要跟着补")
        assertEquals("常用符号", zh.first())
        assertTrue(zh.all { hasHanzi(it) }, "简体下分类名应当都是中文")

        AppLocale.apply(AppLanguage.EN)
        val en = SYMBOL_CATEGORIES.map { it.title }
        assertEquals(44, en.size)
        val leftover = SYMBOL_CATEGORIES.map { it.title }.filter { hasHanzi(it) }
        assertTrue(leftover.isEmpty(), "英文下仍有中文分类名：" + leftover.joinToString())
        assertEquals("Common symbols", en.first())

        AppLocale.apply(AppLanguage.TW)
        val tw = SYMBOL_CATEGORIES.map { it.title }
        assertEquals("常用符號", tw.first())
        assertEquals("愛心符號", tw[2])
        // 繁体里有不少词和简体同形（如「生僻字」），所以只要求大部分被替换过。
        val changed = zh.zip(tw).count { (a, b) -> a != b }
        assertTrue(changed >= 34, "繁体只替换了 $changed 个分类名，疑似漏翻")

        AppLocale.apply(AppLanguage.ZH)
    }
}