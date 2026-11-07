
package com.qingning.sweetlime.ui.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BgEffectBackground(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    bgModifier: Modifier = Modifier,
    isOs3: Boolean = true,
    isDarkTheme: Boolean? = null,
    animate: Boolean = true,
    alpha: () -> Float = { 1f },
    content: @Composable BoxScope.() -> Unit,
) {
    val shaderSupported = remember { isRuntimeShaderSupported() }
    val drawEffect = enabled && shaderSupported

    Box(modifier = modifier) {
        val surface = MiuixTheme.colorScheme.surface

        val painter = remember(isOs3) { BgEffectPainter(isOs3) }
        val animTime = rememberFrameTimeSeconds(animate && drawEffect)
        val isDark = isDarkTheme ?: (MiuixTheme.colorScheme.background.luminance() < 0.5f)
        val preset = remember(isDark, isOs3) { BgEffectConfig.get(isDark, isOs3) }
        val colorStage = remember { Animatable(0f) }

        val periodState = rememberUpdatedState(preset.colorInterpPeriod)
        LaunchedEffect(animate, drawEffect) {
            if (!animate || !drawEffect) return@LaunchedEffect
            var targetStage = colorStage.value + 1f
            while (isActive) {
                delay((periodState.value * 500).toLong())
                colorStage.animateTo(
                    targetValue = targetStage,
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 35f),
                )
                targetStage += 1f
            }
        }

        Canvas(modifier = bgModifier.fillMaxSize()) {

            drawRect(surface)

            val layerAlpha = alpha()
            if (drawEffect && layerAlpha > 0f) {

                val drawHeight = size.height * 0.78f
                val stage = colorStage.value
                val base = stage.toInt()
                val fraction = stage - base
                val getColors = { index: Int ->
                    when (index % 4) {
                        0 -> preset.colors2
                        1 -> preset.colors1
                        2 -> preset.colors2
                        3 -> preset.colors3
                        else -> preset.colors2
                    }
                }
                val start = getColors(base)
                val end = getColors(base + 1)
                painter.updateResolution(size.width, size.height)
                painter.updatePresetIfNeeded(drawHeight, size.height, size.width, isDark)

                painter.updateColors(start, end, fraction)
                painter.updateAnimTime(animTime())
                drawRect(painter.brush, alpha = layerAlpha)
            }
        }

        val flowingLayer = remember(drawEffect, painter, surface) {
            if (drawEffect) FlowingLayer(painter.brush, surface, animTime) else null
        }
        CompositionLocalProvider(
            LocalFlowingLayer provides flowingLayer,
        ) {
            content()
        }
    }
}