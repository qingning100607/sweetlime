package com.qingning.sweetlime.ui.components

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.SinkFeedback

/**
 * 横向滚动条里的小胶囊按钮（「最近使用」用）。
 *
 * 按下时同样走 miuix 的 [SinkFeedback] 下沉手感，和列表行保持一致，
 * 不会出现「列表行有回弹、胶囊没有」这种不一致。
 */
@Composable
fun ChipButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    val source = remember { MutableInteractionSource() }
    val sink = remember {
        SinkFeedback(
            sinkAmount = 0.92f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 1500f),
        )
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .clickable(
                interactionSource = source,
                indication = sink,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
        )
    }
}
