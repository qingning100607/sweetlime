/*
 * 改自 lyricon（Apache License 2.0）的同名文件：
 *   https://github.com/kifranei/lyricon
 *   app/src/main/kotlin/io/github/proify/lyricon/app/compose/effect/BgEffectBackground.kt
 *
 * 按 Apache-2.0 第 4(b) 条声明修改（这是本项目对上游做的改写，同目录其余 .kt 为原样拷贝）：
 *   1. 去掉 HyperOS 大版本探测（上游的 HyperOsDetector）与 SharedPreferences 偏好读取，
 *      风格改成参数 isOs3 传进来 —— 免得为了一个背景把整套偏好框架也搬进来；
 *   2. 参数改名并收敛：dynamicBackground → animate，effectBackground → enabled。
 */

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 「流光背景」：先铺一层主题底色，再用一段 AGSL 着色器（Miuix 的 RuntimeShader）画满整屏 ——
 * 四个彩色光点在 Perlin 噪声上缓慢漂移，配色每隔几秒在几组色板之间用软弹簧插值过去，
 * 就是 HyperOS 系统里那种「流光」观感。
 *
 * 需要 Android 13+（`RuntimeShader`）；机型不支持时自动退化成纯底色，不会崩也不会画错。
 *
 * @param enabled 是否画流光。注意：**关掉时这一层仍然会画主题底色**（它就是这个界面的背景层），
 *   所以外面那层 Scaffold 应该保持透明，否则流光是看不见的。
 * @param isOs3 true = OS3 观感（配色随时间在三组色板间流动，光点动得慢）；
 *   false = OS2 观感（配色固定，四个光点自己绕圈漂移）。
 * @param isDarkTheme 深色/浅色取哪套色板；null（默认）= 按当前主题背景亮度自动判断。
 * @param animate 是否随时间流动。关掉就是一帧静止的流光（省电，也不会每帧重绘）。
 * @param alpha 整层不透明度，默认 1。
 */
@Composable
fun BgEffectBackground(
    enabled: Boolean,
    modifier: Modifier = Modifier,
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

        // 配色不是硬切：每隔 preset.colorInterpPeriod * 500ms 把 stage 加一，
        // 再用一个很软的弹簧插过去，观感就是「颜色慢慢流过去」。
        LaunchedEffect(animate, preset, drawEffect) {
            if (!animate || !drawEffect) return@LaunchedEffect
            var targetStage = 1f
            while (isActive) {
                delay((preset.colorInterpPeriod * 500).toLong())
                colorStage.animateTo(
                    targetValue = targetStage,
                    animationSpec = spring(dampingRatio = 0.9f, stiffness = 35f),
                )
                targetStage += 1f
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // 底色：不管开不开流光，这层都要铺满，它就是界面的背景。
            drawRect(surface)
            if (drawEffect) {
                // 光点按「屏幕高度的 78%」那块区域来分布，剩下的留给顶部大标题区，
                // 免得光斑糊在标题上。
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
                // 配色插值在 painter 内部的预分配缓冲里做，避免每帧新建 FloatArray。
                painter.updateColors(start, end, fraction)
                painter.updateAnimTime(animTime())
                drawRect(painter.brush, alpha = alpha())
            }
        }
        // 把这一帧的「流光画刷 + 底色」交给下面的内容：二级页 / 设置页拿它给自己
        // 再刷一层一模一样的流光，页面就变成「实的」了。详见 FlowingLayer.kt。
        //
        // 这里必须 remember：否则本组件每重组一次都会造一个新的 FlowingLayer，
        // CompositionLocal 的值随之变化 —— 所有读 LocalFlowingLayer 的页面
        // （每个二级页的 flowingPageLayer()）就跟着整棵重组一次，白给的掉帧。
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