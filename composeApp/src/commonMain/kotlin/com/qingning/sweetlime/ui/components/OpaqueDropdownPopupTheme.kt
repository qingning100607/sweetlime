package com.qingning.sweetlime.ui.components

import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun OpaqueDropdownPopupTheme(content: @Composable () -> Unit) {
    val colors = MiuixTheme.colorScheme
    MiuixTheme(
        colors = colors.copy(
            surfaceContainer = colors.surfaceContainer.copy(alpha = 1f),
            surfaceContainerHigh = colors.surfaceContainerHigh.copy(alpha = 1f),
            surfaceContainerHighest = colors.surfaceContainerHighest.copy(alpha = 1f),
            surfaceVariant = colors.surfaceVariant.copy(alpha = 1f),
        ),
        content = content,
    )
}
