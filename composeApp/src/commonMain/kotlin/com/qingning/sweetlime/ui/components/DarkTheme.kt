package com.qingning.sweetlime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 当前主题是不是深色。
 *
 * 移植过来的液态玻璃底栏要按明暗选不同的阴影 / 高光（KernelSU 那边读的是它自己的 UiMode），
 * 这里直接拿主题**背景色**的亮度判断 —— 与 FlowingSurface 判断明暗用的是同一套依据，
 * 所以切主题的那一瞬间也不会用错色板。
 */
@Composable
internal fun isInDarkTheme(): Boolean =
    MiuixTheme.colorScheme.background.luminance() < 0.5f