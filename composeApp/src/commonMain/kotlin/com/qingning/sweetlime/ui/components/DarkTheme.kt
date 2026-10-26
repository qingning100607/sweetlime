package com.qingning.sweetlime.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun isInDarkTheme(): Boolean =
    MiuixTheme.colorScheme.background.luminance() < 0.5f