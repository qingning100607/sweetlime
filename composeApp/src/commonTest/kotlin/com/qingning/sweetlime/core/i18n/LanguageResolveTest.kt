package com.qingning.sweetlime.core.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 「跟随系统」的解析规则。
 *
 * 关键点：**存的 code 和真正生效的语言是两件事**。
 * 选「跟随系统」时生效语言会跟着系统变，但设置页那一行必须还显示「跟随系统」，
 * 所以 [AppLocale.selection] 存 code、[AppLocale.current] 存解析结果，两者不能混。
 */
class LanguageResolveTest {
    @Test
    fun autoFollowsSystemLanguage() {
        assertEquals(AppLanguage.EN, AppLanguage.resolve(AppLanguage.AUTO_CODE, AppLanguage.EN))
        assertEquals(AppLanguage.TW, AppLanguage.resolve(AppLanguage.AUTO_CODE, AppLanguage.TW))
        assertEquals(AppLanguage.ZH, AppLanguage.resolve(AppLanguage.AUTO_CODE, AppLanguage.ZH))
    }

    @Test
    fun blankCodeAlsoMeansFollowSystem() {
        assertEquals(AppLanguage.EN, AppLanguage.resolve("", AppLanguage.EN))
        assertEquals(AppLanguage.TW, AppLanguage.resolve(null, AppLanguage.TW))
    }

    @Test
    fun explicitChoiceWinsOverSystem() {
        assertEquals(AppLanguage.ZH, AppLanguage.resolve("zh", AppLanguage.EN))
        assertEquals(AppLanguage.EN, AppLanguage.resolve("en", AppLanguage.TW))
        assertEquals(AppLanguage.TW, AppLanguage.resolve("tw", AppLanguage.ZH))
    }

    @Test
    fun unknownCodeFallsBackToSimplified() {
        assertEquals(AppLanguage.ZH, AppLanguage.resolve("xx", AppLanguage.EN))
    }

    @Test
    fun selectionAndCurrentAreSeparate() {
        AppLocale.applyStored(AppLanguage.AUTO_CODE, system = AppLanguage.EN)
        assertEquals(AppLanguage.AUTO_CODE, AppLocale.selection)
        assertEquals(AppLanguage.EN, AppLocale.current)

        AppLocale.applyStored("tw", system = AppLanguage.EN)
        assertEquals("tw", AppLocale.selection)
        assertEquals(AppLanguage.TW, AppLocale.current)

        // 老接口仍然表示「明确选了这一种语言」
        AppLocale.apply(AppLanguage.ZH)
        assertEquals("zh", AppLocale.selection)
        assertEquals(AppLanguage.ZH, AppLocale.current)
    }
}