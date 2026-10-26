package com.qingning.sweetlime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.qingning.sweetlime.data.ThemeMode
import com.qingning.sweetlime.ui.effect.ApplySystemBarAppearance
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun SweetLimeTheme(
    monet: Boolean,
    themeMode: Int,
    content: @Composable () -> Unit,
) {
    val mode = resolveMode(monet, themeMode)
    val controller = remember(mode) {
        ThemeController(
            colorSchemeMode = mode,
            colorSpec = ThemeColorSpec.Spec2025,
        )
    }
    // 系统栏（状态栏 / 导航栏）图标颜色跟着**App 自己的**深浅色走。
    // enableEdgeToEdge() 默认是跟系统的，App 单独选深/浅色时状态栏就会“看不见”。
    val dark = controller.isDark ?: isSystemInDarkTheme()
    MiuixTheme(controller = controller) {
                ApplySystemBarAppearance(dark = dark)
        content()
    }
}

private fun resolveMode(monet: Boolean, themeMode: Int): ColorSchemeMode {
    val plain = when (themeMode) {
        ThemeMode.LIGHT -> ColorSchemeMode.Light
        ThemeMode.DARK -> ColorSchemeMode.Dark
        else -> ColorSchemeMode.System
    }
    if (!monet) return plain

    return when (themeMode) {
        ThemeMode.LIGHT -> ColorSchemeMode.MonetLight
        ThemeMode.DARK -> ColorSchemeMode.MonetDark
        else -> ColorSchemeMode.MonetSystem
    }
}