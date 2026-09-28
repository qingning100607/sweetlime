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
 * 按下时整行蒙一层极淡前景色的高亮（浅色模式压暗、深色模式提亮）。
 *
 * 只保留「高亮」这一件事：行的下沉/倾斜由外层负责 —— 见 [PressableRow] 的说明。
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
 * 带按压反馈的列表行 —— 外观与 [BasicComponent] 完全一致。
 *
 * 反馈分两种形态，取决于外面套的是什么：
 *
 * 1. **套在 [TiltPressCard] 里**（列表页的所有卡片）：行自动接手卡片那条按压源，
 *    手指按在哪一行，**整张圆角卡片**就朝按压的那个角沉下去（R 角压下去，
 *    和 lyricon 首页顶部那张激活卡一致）；行自己只留 [pressHighlight] 的高亮，
 *    不再独立缩小，免得和卡片的倾斜两层动画打架。
 * 2. **没有外层卡片**（散落的分隔行）：行自己用 miuix 的 [SinkFeedback] 缩到 0.90
 *    再弹回 —— 就是 HyperOS 原生的那种 sink 手感。
 *
 * 注意：效果只在**按住期间**存在。轻点一下（几十毫秒）几乎看不到，
 * 按住不放才明显，这就是 HyperOS 本身的行为。
 */
@Composable
fun PressableRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    summary: String? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
) {
    // 外层是 TiltPressCard 就用它的源：按下会同时驱动「卡片倾斜」和「本行高亮」。
    val sharedSource = LocalCardPressSource.current
    val source = sharedSource ?: remember { MutableInteractionSource() }
    val sink = remember(sharedSource) {
        if (sharedSource != null) {
            null
        } else {
            // 下沉得更深一点（0.90）+ 更硬更快的弹簧，按下去才「看得见」。
            SinkFeedback(
                sinkAmount = 0.90f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 1500f),
            )
        }
    }

    val row: @Composable () -> Unit = {
        BasicComponent(
            title = title,
            summary = summary,
            endActions = endActions,
            onClick = onClick,
            interactionSource = source,
            modifier = modifier.pressHighlight(source),
        )
    }

    CompositionLocalProvider(LocalIndication provides (sink ?: NoIndication)) {
        row()
    }
}
