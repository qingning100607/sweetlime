
package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme

val LocalFlowingBackground = staticCompositionLocalOf { false }

@Composable
fun FlowingSurface(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        CompositionLocalProvider(LocalFlowingBackground provides false) {
            content()
        }
        return
    }

    val cs = MiuixTheme.colorScheme
    val isDarkBg = cs.background.luminance() < 0.5f

    val cardAlpha = if (isDarkBg) 0.42f else 0.55f

    MiuixTheme(
        colors = cs.copy(

            surfaceContainer = cs.surfaceContainer.copy(alpha = cardAlpha),
            surfaceContainerHigh = cs.surfaceContainerHigh.copy(alpha = cardAlpha),
            surfaceContainerHighest = cs.surfaceContainerHighest.copy(alpha = cardAlpha),
            surfaceVariant = cs.surfaceVariant.copy(alpha = cardAlpha),
            dividerLine = Color.Transparent,
        ),
    ) {
        CompositionLocalProvider(LocalFlowingBackground provides true) {
            content()
        }
    }
}
