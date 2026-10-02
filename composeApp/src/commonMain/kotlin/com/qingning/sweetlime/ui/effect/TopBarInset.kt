package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 当前页面「顶栏浮层」的高度。
 *
 * 顶栏（Haze 实时模糊那一条）是**浮层**，盖在内容之上。页面想要那种
 * 「内容从顶栏下面滑过去、被逐帧糊掉」的效果，就必须把这点高度写进
 * **滚动内容里面**（`contentPadding` 或滚动内容里的第一个 Spacer）；
 *
 * 如果写成「外层占位 + 内容从下面开始」，内容永远滑不到顶栏下面
 * → 采样层里只有一片平色 → 看着就像顶栏没有模糊（二级页之前的毛病）。
 */
val LocalTopBarInset = compositionLocalOf { 0.dp }

/** 在滚动内容的最上面让出「顶栏高度」。必须放在滚动容器**内部**。 */
@Composable
fun TopBarInsetSpacer() {
    val inset = LocalTopBarInset.current
    if (inset > 0.dp) {
        Spacer(modifier = Modifier.height(inset))
    }
}
