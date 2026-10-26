
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
    // 暗色底下流光本身就亮，卡片要压得更实一点才读得清字；浅色则反过来，敢多透一些。
    val cardAlpha = if (isDarkBg) 0.42f else 0.55f

    MiuixTheme(
        colors = cs.copy(
            // 卡片 / 列表项默认取的就是这几档：这几档必须留住一点白，字才读得清。
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
