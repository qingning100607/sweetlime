package com.qingning.sweetlime.core.mapping

import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.AppLocale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 颜文字库的自检。
 *
 * 这批数据的错法很隐蔽：重复一条、少一个括号、混进一个替换字符，
 * 肉眼扫一遍根本看不出来，所以硬性质全部交给测试守住。
 *
 * 注意：**括号数量不对称是合法的** —— 颜文字本来就有一堆故意只开不闭的写法
 * （`（￣▽￣"）`、`∪.∪ )...zzz`、`(¦3[▓▓]`、`Σ(°Д°;` 都是经典款），所以这里不查括号。
 */
class KaomojiDataTest {

    private fun hasHanzi(text: String): Boolean = text.any { it in '\u4e00'..'\u9fff' }

    @Test
    fun categoriesAndItemsAreNotEmpty() {
        assertTrue(KAOMOJI_CATEGORIES.size >= 10, "分类太少，看着像数据没加载出来")
        KAOMOJI_CATEGORIES.forEach { category ->
            assertTrue(category.items.isNotEmpty(), "分类「${category.title}」是空的")
        }
    }

    @Test
    fun itemsAreUniqueAndClean() {
        val all = KAOMOJI_CATEGORIES.flatMap { it.items }
        assertEquals(all.size, all.toSet().size, "有重复的颜文字")
        all.forEach { item ->
            assertTrue(item.isNotBlank(), "有空项")
            assertEquals(item, item.trim(), "首尾有空白：$item")
            assertFalse(item.contains('\uFFFD'), "含替换字符（多半是编码坏了）：$item")
            assertTrue(item.length <= 48, "太长了，可能是拼错了：$item")
            assertFalse(
                item.any { it.code < 0x20 || it.code == 0x7F },
                "含控制字符：$item",
            )
        }
    }

    @Test
    fun libraryIsBigEnough() {
        val total = KAOMOJI_CATEGORIES.sumOf { it.items.size }
        assertTrue(total >= 100, "颜文字只有 $total 条，太少了")
    }

    @Test
    fun titlesFollowLanguage() {
        AppLocale.apply(AppLanguage.ZH)
        val zh = KAOMOJI_CATEGORIES.map { it.title }
        assertTrue(zh.all { hasHanzi(it) }, "简体下分类名应当都是中文")

        AppLocale.apply(AppLanguage.EN)
        val leftover = KAOMOJI_CATEGORIES.map { it.title }.filter { hasHanzi(it) }
        assertTrue(leftover.isEmpty(), "英文下仍有中文分类名：" + leftover.joinToString())
        assertEquals("Happy", KAOMOJI_CATEGORIES.first().title)

        AppLocale.apply(AppLanguage.TW)
        val tw = KAOMOJI_CATEGORIES.map { it.title }
        val changed = zh.zip(tw).count { (a, b) -> a != b }
        assertTrue(changed >= 9, "繁体只替换了 $changed 个分类名，疑似漏翻")
        assertEquals("開心", tw.first())

        AppLocale.apply(AppLanguage.ZH)
    }
}