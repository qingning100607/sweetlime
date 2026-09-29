package com.qingning.sweetlime.core.i18n

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.tools.BmiLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * 「读时才翻译」的回归测试。
 *
 * 这一类文案踩过同一个坑：把 tr() 的返回值当成构造参数 / 初始化式存下来，
 * 于是语言被定死成「第一次访问那一刻」的语言，之后切语言再也不生效。
 *
 * 涉及的每一处都要在这里盯住：
 * - [TransformRegistry] 里的样式标题 —— 样式对象只构造一次（all 是个 val）；
 * - [BmiLevel] 的枚举常量 —— 枚举初始化只有一次；
 * - [com.qingning.sweetlime.core.StyleGroup] —— 早就改对了，留作对照组。
 */
class LazyTranslationTest {

    private fun titleOf(id: String): String {
        val style = TransformRegistry.byId(id)
        assertNotNull(style, "样式 $id 不存在")
        return style.title
    }

    @Test
    fun styleTitleFollowsLanguageEvenAfterRegistryInit() {
        // 先把注册表读一遍：模拟「应用启动时就已经构造过样式对象」。
        // 旧实现里这一步会把标题定死成中文。
        AppLocale.apply(AppLanguage.ZH)
        assertEquals("粗体", titleOf("latin_bold"))

        // 切语言后，同一个样式对象也必须跟着变。
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

        // 纯数字区间在词表里没有条目，应当原样返回，不能被翻译成空串。
        assertEquals("18.5 – 23.9", BmiLevel.NORMAL.range)

        AppLocale.apply(AppLanguage.ZH)
    }

    @Test
    fun byIdFindsEveryRegisteredStyle() {
        // 加索引之后不允许任何一个 id 查不到。
        for (style in TransformRegistry.all) {
            assertEquals(style, TransformRegistry.byId(style.id), "byId 查不到 ${style.id}")
        }
        assertEquals(null, TransformRegistry.byId("不存在的样式id"))
    }

    /** 一个字符串里有没有汉字。 */
    private fun hasHanzi(text: String): Boolean =
        text.any { it in '\u4e00'..'\u9fff' }

    @Test
    fun noStyleTitleStaysChineseInEnglish() {
        // 这是「特效符号 / 花样网名 / 翅膀装饰 / 花藤字」那一大票装饰模板的兜底：
        // 它们由 TemplateStyle 自带的类构造，一旦标题在构造时就被定死，
        // 切到英文后整片列表都还是中文（用户实际报过这个）。
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
