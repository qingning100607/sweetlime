package com.qingning.sweetlime.data

import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 「刚装完那一版」的默认设置要固定住，别再被顺手改掉。
 *
 * 首装的四个开关是产品决定，不是随手写下的初值：
 * - 流光背景 **关**（首帧/每帧的着色器开销不该让新用户先扛）
 * - 震动反馈 **开**
 * - 悬浮底栏 **关**（默认用贴底的标准导航栏）
 * - 主题 **浅色**
 *
 * 这里的 store 是「什么都没有」的状态，正好等价于刚安装完的 SharedPreferences。
 */
class SweetLimeSettingsTest {

    /** 空 store：不存任何值，一律回落默认值 —— 等价于首次安装。 */
    private class EmptyStore : KeyValueStore {
        override fun getString(key: String, defaultValue: String): String = defaultValue
        override fun putString(key: String, value: String) = Unit
        override fun getStringSet(key: String): Set<String> = emptySet()
        override fun putStringSet(key: String, values: Set<String>) = Unit
        override fun getBoolean(key: String, defaultValue: Boolean) = defaultValue
        override fun putBoolean(key: String, value: Boolean) = Unit
        override fun getInt(key: String, defaultValue: Int) = defaultValue
        override fun putInt(key: String, value: Int) = Unit
    }

    @Test
    fun firstInstallDefaults() {
        val settings = SweetLimeSettings(EmptyStore())

        assertFalse(settings.flowingBackground, "流光背景首装应该是关的")
        assertTrue(settings.hapticFeedback, "震动反馈首装应该是开的")
        assertFalse(settings.floatingBottomBar, "悬浮底栏首装应该是关的（用贴底标准底栏）")
        assertEquals(ThemeMode.LIGHT, settings.themeMode, "首装应该是浅色模式")
        // 顺带钉住其余几项，免得以后误改
        assertEquals("zh", settings.language)
        assertFalse(settings.monet)
        assertEquals(HyperOsStyle.OS3, settings.flowingStyle)
        assertEquals("", settings.dismissedUpdate)
    }

    @Test
    fun valuesAreWrittenBack() {
        // 改过的值要能落盘：换个实例从同一个 store 读回来还是改过的那个。
        val store = RecordingStore()
        val first = SweetLimeSettings(store)
        first.flowingBackground = true
        first.themeMode = ThemeMode.DARK
        first.hapticFeedback = false
        first.floatingBottomBar = true

        val second = SweetLimeSettings(store)
        assertTrue(second.flowingBackground)
        assertEquals(ThemeMode.DARK, second.themeMode)
        assertFalse(second.hapticFeedback)
        assertTrue(second.floatingBottomBar)
    }

    /** 会真的记住写入值的 store，用来验证「设置了就落盘」。 */
    private class RecordingStore : KeyValueStore {
        private val booleans = mutableMapOf<String, Boolean>()
        private val ints = mutableMapOf<String, Int>()
        private val strings = mutableMapOf<String, String>()

        override fun getString(key: String, defaultValue: String): String = strings[key] ?: defaultValue
        override fun putString(key: String, value: String) {
            strings[key] = value
        }

        override fun getStringSet(key: String): Set<String> = emptySet()
        override fun putStringSet(key: String, values: Set<String>) = Unit
        override fun getBoolean(key: String, defaultValue: Boolean): Boolean = booleans[key] ?: defaultValue
        override fun putBoolean(key: String, value: Boolean) {
            booleans[key] = value
        }

        override fun getInt(key: String, defaultValue: Int): Int = ints[key] ?: defaultValue
        override fun putInt(key: String, value: Int) {
            ints[key] = value
        }
    }
}