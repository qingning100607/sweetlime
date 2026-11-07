package com.qingning.sweetlime.ui.components

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.drawBackdrop
import com.qingning.sweetlime.core.softBlurEffect
import top.yukonga.miuix.kmp.blur.effect

fun Modifier.glassBar(
    backdrop: Backdrop,
    blurPx: Float,
    tint: Color,
    shape: Shape = RectangleShape,
    fadeFromTop: Boolean? = null,
    tintAlpha: Float = 0.32f,
    saturation: Float = 1.12f,
    brightness: Float = 1.04f,
): Modifier = this
    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            padding = blurPx * 2f
            effect(softBlurEffect(blurPx, saturation, brightness))
        },
    )
    .background(color = tint.copy(alpha = tintAlpha), shape = shape)
    .then(
        if (fadeFromTop == null) {
            Modifier
        } else {
            Modifier.drawWithContent {
                drawContent()
                val mask = if (fadeFromTop) {
                    Brush.verticalGradient(
                        0.0f to Color.Black,
                        0.5f to Color.Black,
                        1.0f to Color.Transparent,
                    )
                } else {

                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.35f to Color.Black,
                        1.0f to Color.Black,
                    )
                }
                drawRect(brush = mask, blendMode = BlendMode.DstIn)
            }
        },
    )
