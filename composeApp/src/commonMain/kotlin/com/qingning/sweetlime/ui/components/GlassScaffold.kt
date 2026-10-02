package com.qingning.sweetlime.ui.components

import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.ui.effect.flowingPageLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 二级页的通用外壳：**带柔光玻璃的顶栏 + 从玻璃下面穿过去的内容**。
 *
 * 内容层自己负责滚动（[content] 拿到的是「顶栏挡住了多高」，可以先垫一个等高的 Spacer，
 * 也可以直接当 contentPadding 用），顶栏永远是浮在上面的那一层。
 */
@Composable
fun GlassTopBarScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable (topPadding: Dp) -> Unit,
) {
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    // 顶栏的「实时模糊」走 Haze：内容层是 source、顶栏是 effect（逐帧真高斯 + 渐变）。
    val hazeState = remember { HazeState() }
    val tint = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tint).flowingPageLayer(),
    ) {
        // 内容层：Haze 的采样源。顶栏是它的**兄弟**节点（不是子节点），
        // 所以列表从顶栏下面滑过去时会被实时糊掉。
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 采样源里先铺一层「和整页一样的底」：不然采样区是透明的，
                // 那片区域就等于没糊 —— 之前设置页顶栏看着比主页“淡”就是这个原因。
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {
            content(topBarHeight)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 20.dp,
                            noiseFactor = 0.15f,
                            tint = HazeTint(tint.copy(alpha = if (flowing) 0.16f else 0.30f)),
                        ),
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
            )
            SmallTopAppBar(
                title = title,
                color = Color.Transparent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = tr("返回"),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = actions ?: {},
            )
        }
    }
}
