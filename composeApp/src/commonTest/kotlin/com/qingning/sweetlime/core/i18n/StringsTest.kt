package com.qingning.sweetlime.core.i18n

import com.qingning.sweetlime.core.StyleGroup
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 三语词表的自检。
 *
 * 重点不是「译文好不好」，而是三条底线：
 * 1. 简体是源语言，必须原样返回；
 * 2. 查不到的 key 必须回落成原文（不能变成空白、更不能抛异常）；
 * 3. 模板串的参数要按顺序填回去，且译文里的 `{}` 数量要和原文一致。
 */
class StringsTest {

    @Test
    fun simplifiedChineseIsTheSource() {
        AppLocale.apply(AppLanguage.ZH)
        assertEquals("设置", tr("设置"))
        assertEquals("正在下载 42%", trf("正在下载 {}%", 42))
    }

    @Test
    fun englishTranslatesCommonRows() {
        AppLocale.apply(AppLanguage.EN)
        assertEquals("Settings", tr("设置"))
        assertEquals("Tools", tr("工具"))
        assertEquals("Favorites", tr("收藏"))
        assertEquals("Check for updates", tr("检查更新"))
    }

    @Test
    fun traditionalTranslatesCommonRows() {
        AppLocale.apply(AppLanguage.TW)
        assertEquals("設定", tr("设置"))
        assertEquals("工具", tr("工具"))
        assertEquals("搜尋", tr("搜索"))
        assertEquals("檢查更新", tr("检查更新"))
    }

    @Test
    fun unknownKeyFallsBackToSource() {
        AppLocale.apply(AppLanguage.EN)
        assertEquals("这段文案没有翻译", tr("这段文案没有翻译"))
        AppLocale.apply(AppLanguage.TW)
        assertEquals("这段文案没有翻译", tr("这段文案没有翻译"))
    }

    @Test
    fun templateKeepsPlaceholderOrder() {
        AppLocale.apply(AppLanguage.EN)
        assertEquals("Downloading 7%", trf("正在下载 {}%", 7))
        assertEquals("Version 2.6.8 (no update source configured)",
            trf("当前版本 {}（更新源未配置）", "2.6.8"))
    }

    @Test
    fun tablesHaveNoBlankValues() {
        AppLocale.apply(AppLanguage.ZH)
        for ((key, value) in EN) {
            assertTrue(value.isNotBlank(), "英文译文为空：$key")
        }
        for ((key, value) in TW) {
            assertTrue(value.isNotBlank(), "繁体译文为空：$key")
        }
        // 两张表的 key 应该完全一致，缺一个就说明漏加了。
        assertEquals(EN.keys, TW.keys)
    }

    @Test
    fun placeholdersMatchBetweenSourceAndTranslation() {
        fun marks(s: String) = Regex("\\{\\}").findAll(s).count()
        for (table in listOf(EN, TW)) {
            for ((key, value) in table) {
                assertEquals(marks(key), marks(value), "占位符数量不一致：$key")
            }
        }
    }

    @Test
    fun styleGroupLabelFollowsLanguage() {
        AppLocale.apply(AppLanguage.ZH)
        assertEquals("英文花体", StyleGroup.LATIN.label)
        AppLocale.apply(AppLanguage.TW)
        assertEquals("英文花體", StyleGroup.LATIN.label)
        AppLocale.apply(AppLanguage.EN)
        assertEquals("Latin fancy", StyleGroup.LATIN.label)
        AppLocale.apply(AppLanguage.ZH)
    }

    @Test
    fun languageCodeRoundTrip() {
        assertEquals(AppLanguage.TW, AppLanguage.fromCode("tw"))
        assertEquals(AppLanguage.EN, AppLanguage.fromCode("en"))
        // 存坏了也不能崩，回落简体。
        assertEquals(AppLanguage.ZH, AppLanguage.fromCode("ja"))
        assertEquals(AppLanguage.ZH, AppLanguage.fromCode(null))
    }
}