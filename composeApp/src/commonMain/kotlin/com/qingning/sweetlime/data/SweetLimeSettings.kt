package com.qingning.sweetlime.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.ui.effect.HyperOsStyle

object ThemeMode {
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2
}

class SweetLimeSettings(private val store: KeyValueStore) {
    private var monetState by mutableStateOf(store.getBoolean(KEY_MONET, false))
    private var themeModeState by mutableStateOf(store.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM))

    private var floatingBarState by mutableStateOf(store.getBoolean(KEY_FLOATING_BAR, false))

    private var dismissedUpdateState by mutableStateOf(store.getString(KEY_DISMISSED_UPDATE, ""))

    private var flowingBgState by mutableStateOf(store.getBoolean(KEY_FLOWING_BG, false))

    private var flowingStyleState by mutableStateOf(store.getInt(KEY_FLOWING_STYLE, HyperOsStyle.AUTO))
    private var hapticState by mutableStateOf(store.getBoolean(KEY_HAPTIC, true))
    private var floatingBarStyleState by mutableStateOf(store.getInt(KEY_FLOATING_BAR_STYLE, 0))

    private var languageState by mutableStateOf(store.getString(KEY_LANGUAGE, "zh"))

    var monet: Boolean
        get() = monetState
        set(value) {
            monetState = value
            store.putBoolean(KEY_MONET, value)
        }

    var themeMode: Int
        get() = themeModeState
        set(value) {
            themeModeState = value
            store.putInt(KEY_THEME_MODE, value)
        }

    var floatingBottomBar: Boolean
        get() = floatingBarState
        set(value) {
            floatingBarState = value
            store.putBoolean(KEY_FLOATING_BAR, value)
        }

    var dismissedUpdate: String
        get() = dismissedUpdateState
        set(value) {
            dismissedUpdateState = value
            store.putString(KEY_DISMISSED_UPDATE, value)
        }

    var flowingBackground: Boolean
        get() = flowingBgState
        set(value) {
            flowingBgState = value
            store.putBoolean(KEY_FLOWING_BG, value)
        }

    var flowingStyle: Int
        get() = flowingStyleState
        set(value) {
            flowingStyleState = value
            store.putInt(KEY_FLOWING_STYLE, value)
        }

    var floatingBarStyle: Int
        get() = floatingBarStyleState
        set(value) {
            floatingBarStyleState = value
            store.putInt(KEY_FLOATING_BAR_STYLE, value)
        }
    var hapticFeedback: Boolean
        get() = hapticState
        set(value) {
            hapticState = value
            store.putBoolean(KEY_HAPTIC, value)
        }

    var language: String
        get() = languageState
        set(value) {
            languageState = value
            store.putString(KEY_LANGUAGE, value)
        }

    private companion object {
        const val KEY_MONET = "theme_monet"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FLOATING_BAR = "floating_bottom_bar"
        const val KEY_DISMISSED_UPDATE = "dismissed_update_version"
        const val KEY_FLOWING_BG = "flowing_background"
        const val KEY_FLOWING_STYLE = "flowing_background_style"
        const val KEY_LANGUAGE = "language"
        const val KEY_HAPTIC = "haptic_feedback"
        const val KEY_FLOATING_BAR_STYLE = "floating_bar_style"
    }
}
