package com.qingning.sweetlime.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.ui.effect.HyperOsStyle

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
    // 主页「发现新版本」提示被点掉的那个版本号：同一个版本不再打扰，
    // 等出了更新的一版会重新提示（空串 = 没有点掉过任何版本）。
    private var dismissedUpdateState by mutableStateOf(store.getString(KEY_DISMISSED_UPDATE, ""))
    // 流光背景：默认关闭（保持原来的纯色背景），想要的自己去设置里开。
    private var flowingBgState by mutableStateOf(store.getBoolean(KEY_FLOWING_BG, false))
    // 流光风格：0=跟随系统（按 HyperOS 大版本）、1=OS2、2=OS3。见 HyperOsStyle。
    private var flowingStyleState by mutableStateOf(store.getInt(KEY_FLOWING_STYLE, HyperOsStyle.AUTO))

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

    /** 主页顶部「发现新版本」提示里点过「×」的版本号；同一个版本不再提示。 */
    var dismissedUpdate: String
        get() = dismissedUpdateState
        set(value) {
            dismissedUpdateState = value
            store.putString(KEY_DISMISSED_UPDATE, value)
        }

    /** 流光背景：整屏铺一层慢速流动的彩色光晕（着色器实现，需要 Android 13+）。 */
    var flowingBackground: Boolean
        get() = flowingBgState
        set(value) {
            flowingBgState = value
            store.putBoolean(KEY_FLOWING_BG, value)
        }

    /**
     * 流光风格：跟随系统 / OS2 / OS3（取值见 [com.qingning.sweetlime.ui.effect.HyperOsStyle]）。
     *
     * 「跟随系统」按 HyperOS 大版本自动选；手动选了就无视系统版本。
     */
    var flowingStyle: Int
        get() = flowingStyleState
        set(value) {
            flowingStyleState = value
            store.putInt(KEY_FLOWING_STYLE, value)
        }

    private companion object {
        const val KEY_MONET = "theme_monet"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_FLOATING_BAR = "floating_bottom_bar"
        const val KEY_DISMISSED_UPDATE = "dismissed_update_version"
        const val KEY_FLOWING_BG = "flowing_background"
        const val KEY_FLOWING_STYLE = "flowing_background_style"
    }
}
