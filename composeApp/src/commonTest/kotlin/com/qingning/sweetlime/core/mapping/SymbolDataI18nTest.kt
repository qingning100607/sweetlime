package com.qingning.sweetlime.core.mapping

import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.AppLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SymbolDataI18nTest {

    private fun hasHanzi(text: String): Boolean = text.any { it in '\u4e00'..'\u9fff' }

    @Test
    fun categoryTitlesFollowLanguage() {
        AppLocale.apply(AppLanguage.ZH)
        val zh = SYMBOL_CATEGORIES.slice(SYMBOL_BASE_RANGE).map { it.title }
        assertEquals(44, zh.size, "分类数量变了，词表要跟着补")
        assertEquals("常用符号", zh.first())
        assertTrue(zh.all { hasHanzi(it) }, "简体下分类名应当都是中文")

        AppLocale.apply(AppLanguage.EN)
        val en = SYMBOL_CATEGORIES.slice(SYMBOL_BASE_RANGE).map { it.title }
        assertEquals(44, en.size)
        val leftover = SYMBOL_CATEGORIES.map { it.title }.filter { hasHanzi(it) }
        assertTrue(leftover.isEmpty(), "英文下仍有中文分类名：" + leftover.joinToString())
        assertEquals("Common symbols", en.first())

        AppLocale.apply(AppLanguage.TW)
        val tw = SYMBOL_CATEGORIES.slice(SYMBOL_BASE_RANGE).map { it.title }
        assertEquals("常用符號", tw.first())
        assertEquals("愛心符號", tw[2])
        val changed = zh.zip(tw).count { (a, b) -> a != b }
        assertTrue(changed >= 34, "繁体只替换了 $changed 个分类名，疑似漏翻")

        assertTrue(KAOMOJI_RANGE.first > SYMBOL_MORE_RANGE.last, "颜文字分类应当排在扩展符号之后")

        AppLocale.apply(AppLanguage.ZH)
    }
}