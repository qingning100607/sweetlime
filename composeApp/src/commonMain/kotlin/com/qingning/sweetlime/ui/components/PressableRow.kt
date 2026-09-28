package com.qingning.sweetlime.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SinkFeedback

/**
 * HyperOS / KernelSU 那种「按下去」的触感。
 *
 * 两个叠加的效果，保证一定能看出来：
 * 1. **下沉**：手指按住时整行缩到 0.94 再带弹簧弹回 —— 直接用 miuix 自带的
 *    [SinkFeedback]（就是 HyperOS 原生的那种 sink 手感），通过 [LocalIndication]
 *    交给 [BasicComponent] 内部的 clickable，按下 / 抬起 / 滑出取消都由它自己正确处理；
 * 2. **高亮**：同时整行蒙一层极淡的前景色（浅色模式压暗、深色模式提亮），
 *    在绘制阶段读动画值，不触发重组。
 *
 * 注意：效果只在**按住期间**存在。轻点一下（几十毫秒）几乎看不到 —— 按住不放才明显，
 * 这就是 HyperOS 本身的行为。
 */
@Composable
fun Modifier.pressHighlight(
    interactionSource: InteractionSource,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val progress by animateFloatAsState(
        targetValue = if (pressed) 1f else 0f,
        // 反应要快：轻点一下（几十毫秒）也要能看到那一下高亮闪动，
        // 阻尼略低，抬起来时会有一点点回弹，手感更「实」。
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pressHighlight",
    )
    val highlight = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.13f)
    return this.drawBehind {
        val p = progress
        if (p > 0.001f) {
            drawRect(color = highlight, alpha = highlight.alpha * p)
        }
    }
}

/**
 * 带「按下下沉」触感的列表行 —— 外观与 [BasicComponent] 完全一致，
 * 只是把按下状态接到 [SinkFeedback] + [pressHighlight] 上。
 */
@Composable
fun PressableRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    // 下沉得更深一点（0.90）+ 更硬更快的弹簧，按下去才「看得见」。
    val sink = remember {
        SinkFeedback(
            sinkAmount = 0.90f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 1500f),
        )
    }
    CompositionLocalProvider(LocalIndication provides sink) {
        BasicComponent(
            title = title,
            summary = summary,
            endActions = endActions,
            onClick = onClick,
            interactionSource = source,
            modifier = modifier.pressHighlight(source),
        )
    }
}