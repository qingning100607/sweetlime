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

/**
 * 磨砂玻璃 + 淡出：先按 backdrop 画一层全分辨率的高斯模糊，叠一层很淡的底色，
 * 再用一条垂直 alpha 渐变把它遮掉一部分。
 *
 * 为什么不用 miuix 自带的 blur()：
 * 它为了性能会自适应降采样（把内容录到 1/2 ~ 1/16 分辨率的离屏纹理上再模糊），
 * 半径一大，文字就会被糊成一块块的马赛克 / 像素感，深色模式下尤其明显。
 * 这里改成把原生 RenderEffect 的高斯模糊（BlurEffect）直接挂到 backdrop 的
 * renderEffect 上：图层按全分辨率录制、全分辨率模糊，所以再怎么糊都是柔的。
 *
 * [shape] 玻璃的形状（顶栏 / 底栏是矩形，悬浮底栏是圆角胶囊）；
 * [fadeFromTop] = true（顶栏）顶边最实往下变淡、= false（底栏）上边透明往下变实、
 * = null（悬浮胶囊）整块实心不做渐隐。
 */
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
                    // 底栏：只在最上面一小截渐隐（原来的 0.55 太长，几乎半条栏都是透的），
                    // 剩下的部分保持「实心磨砂」。
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
