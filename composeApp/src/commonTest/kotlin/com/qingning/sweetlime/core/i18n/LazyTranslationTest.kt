package com.qingning.sweetlime.core.i18n

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.tools.BmiLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LazyTranslationTest {

    private fun titleOf(id: String): String {
        val style = TransformRegistry.byId(id)
        assertNotNull(style, "样式 $id 不存在")
        return style.title
    }

    @Test
    fun styleTitleFollowsLanguageEvenAfterRegistryInit() {

        AppLocale.apply(AppLanguage.ZH)
        assertEquals("粗体", titleOf("latin_bold"))

        AppLocale.apply(AppLanguage.EN)
        assertEquals("Bold", titleOf("latin_bold"))

        AppLocale.apply(AppLanguage.TW)
        assertEquals("粗體", titleOf("latin_bold"))

        AppLocale.apply(AppLanguage.ZH)
    }

    @Test
    fun bmiLevelFollowsLanguage() {
        AppLocale.apply(AppLanguage.ZH)
        assertEquals("偏瘦", BmiLevel.THIN.label)

        AppLocale.apply(AppLanguage.EN)
        assertEquals("Underweight", BmiLevel.THIN.label)

        assertEquals("18.5 – 23.9", BmiLevel.NORMAL.range)

        AppLocale.apply(AppLanguage.ZH)
    }

    @Test
    fun byIdFindsEveryRegisteredStyle() {

        for (style in TransformRegistry.all) {
            assertEquals(style, TransformRegistry.byId(style.id), "byId 查不到 ${style.id}")
        }
        assertEquals(null, TransformRegistry.byId("不存在的样式id"))
    }

    private fun hasHanzi(text: String): Boolean =
        text.any { it in '\u4e00'..'\u9fff' }

    @Test
    fun noStyleTitleStaysChineseInEnglish() {

        AppLocale.apply(AppLanguage.EN)

        val leftover = TransformRegistry.all.filter { hasHanzi(it.title) }
        assertTrue(
            leftover.isEmpty(),
            "英文下仍有中文样式标题：" + leftover.joinToString { "${it.id}=${it.title}" },
        )

        val groups = StyleGroup.entries.filter { hasHanzi(it.label) }
        assertTrue(
            groups.isEmpty(),
            "英文下仍有中文分组名：" + groups.joinToString { "${it.name}=${it.label}" },
        )

        AppLocale.apply(AppLanguage.ZH)
    }
}
