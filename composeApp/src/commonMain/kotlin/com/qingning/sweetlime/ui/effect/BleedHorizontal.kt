package com.qingning.sweetlime.ui.effect

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 让一个铺满的元素**左右各多出 [extra]**（画出去的部分由窗口自己裁掉）。
 *
 * 顶栏玻璃用它：过渡动画里页面在平移，backdrop 采样的边界如果正好落在
 * 屏幕内，最边上那一窄条就会「没糊」—— 白色/文字直接透出来。
 * 把它画宽一点，边界就移到屏幕外了。
 */
fun Modifier.bleedHorizontal(extra: Dp = 24.dp): Modifier = this.layout { measurable, constraints ->
    val px = extra.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.maxWidth + px * 2,
            maxWidth = constraints.maxWidth + px * 2,
        ),
    )
    layout(constraints.maxWidth, placeable.height) { placeable.place(-px, 0) }
}