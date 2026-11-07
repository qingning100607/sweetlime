package com.qingning.sweetlime.data

import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SweetLimeSettingsTest {

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
        assertEquals(ThemeMode.SYSTEM, settings.themeMode, "首装应该是跟随系统")

        assertEquals("zh", settings.language)
        assertFalse(settings.monet)
        assertEquals(HyperOsStyle.AUTO, settings.flowingStyle)
        assertEquals("", settings.dismissedUpdate)
    }

    @Test
    fun valuesAreWrittenBack() {

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