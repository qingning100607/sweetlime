package com.qingning.sweetlime.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
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

    val dark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        else -> isSystemInDarkTheme()
    }
    MiuixTheme(controller = controller) {
        ApplySystemBarAppearance(dark = dark)
        if (!dark && !monet) {
            val colors = MiuixTheme.colorScheme
            MiuixTheme(colors = colors.copy(surface = LIGHT_SURFACE, background = LIGHT_SURFACE)) {
                content()
            }
        } else {
            content()
        }
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

private val LIGHT_SURFACE = Color(0xFFF0F1F2)
