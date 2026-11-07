
package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme

class FlowingLayer(
    val brush: Brush,
    val base: Color,

    val tick: () -> Float,
)

val LocalFlowingLayer = staticCompositionLocalOf<FlowingLayer?> { null }

@Composable
fun Modifier.flowingPageBackground(tint: Color): Modifier {
    val layer = LocalFlowingLayer.current ?: return this.background(tint)
    return drawBehind {

        layer.tick()

        drawRect(tint)
        drawRect(layer.brush)
    }
}

@Composable
fun Modifier.flowingPageLayer(alpha: () -> Float = { 1f }): Modifier {
    val layer = LocalFlowingLayer.current ?: return this
    return drawBehind {
        layer.tick()
        val a = alpha().coerceIn(0f, 1f)
        if (a <= 0f) return@drawBehind
        drawRect(layer.base, alpha = a)
        drawRect(layer.brush, alpha = a)
    }
}

@Composable
fun Modifier.pageBackdropLayer(): Modifier {
    val flowing = LocalFlowingBackground.current
    if (!flowing) {

        return this.background(MiuixTheme.colorScheme.surface)
    }
    val layer = LocalFlowingLayer.current ?: return this.background(MiuixTheme.colorScheme.surface)

    return this.flowingPageLayer()
}

@Composable
fun Modifier.glassBarBackdrop(): Modifier {

    return this.flowingPageLayer()
}
