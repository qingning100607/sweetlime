package com.qingning.sweetlime.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.KeyValueStore

/** 深色模式选项。 */
object ThemeMode {
    const val SYSTEM = 0
    const val LIGHT = 1
    const val DARK = 2
}

/**
 * 用户设置。字段用 Compose 的 mutableStateOf 承载，读写即触发重组，
 * 赋值时顺便落盘（SharedPreferences 是异步 apply，不会卡 UI）。
 */
class SweetLimeSettings(private val store: KeyValueStore) {
    private var monetState by mutableStateOf(store.getBoolean(KEY_MONET, false))
    private var themeModeState by mutableStateOf(store.getInt(KEY_THEME_MODE, ThemeMode.SYSTEM))
    // 默认关闭：保持原有的贴底导航栏，想要的自己去设置里开。
    private var floatingBarState by mutableStateOf(store.getBoolean(KEY_FLOATING_BAR, false))

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

    /** 悬浮底栏：抄 KernelSU 那种「浮起来的小圆角胶囊」。 */
    var floatingBottomBar: Boolean
        get() = floatingBarState
        set(value) {
            floatingBarState = value
            store.putBoolean(KEY_FLOATING_BAR, value)
        }

    private companion object {
        const val KEY_MONET = "theme_monet"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FLOATING_BAR = "floating_bottom_bar"
    }
}
