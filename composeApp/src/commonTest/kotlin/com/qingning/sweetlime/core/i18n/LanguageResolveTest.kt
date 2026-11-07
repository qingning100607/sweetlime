package com.qingning.sweetlime.core.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

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

        AppLocale.apply(AppLanguage.ZH)
        assertEquals("zh", AppLocale.selection)
        assertEquals(AppLanguage.ZH, AppLocale.current)
    }
}