package com.qingning.sweetlime.ui.components

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
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
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
    val backdrop = rememberLayerBackdrop()
    val blurPx = remember(density) { with(density) { 24.dp.toPx() } }
    val tint = MiuixTheme.colorScheme.surface
    // 流光模式下顶栏不做玻璃，让底层流光透上来（对齐上游 lyricon 的 hazeState = null）。
    val flowing = LocalFlowingBackground.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(tint).flowingPageLayer(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
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
                    // 流光模式下顶栏不做玻璃（上游 lyricon 就是把 haze 模糊整个关掉的：
                    // 顶栏保持全透明，让底层流光直接透上来），只有非流光模式才铺这层玻璃。
                    // 注意流光时这里**什么都不画**：整屏的实流光已经由外层 Box 铺好了，
                    // 顶栏这一条本身就在它上面，直接透上来即可（详见下面的注释）。
                    .then(
                        if (flowing) {
                            // 整页的流光已经由外层 Box 铺满了（顶栏这一条也在它下面），
                            // 这里千万不要再画一层：flowingPageLayer 的画刷是按「整屏」
                            // 取样出来的，塞进顶栏这一小条里只会取到画刷偏亮的那一段，
                            // 顶栏就变成一条发白的横条（上滑时尤其明显）。
                            // 所以流光模式下这里什么都不画，让外层那层实流光直接透上来。
                            Modifier
                        } else {
                            Modifier.glassBar(backdrop, blurPx, tint, fadeFromTop = true)
                        },
                    ),
            )
            SmallTopAppBar(
                title = title,
                color = Color.Transparent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
                actions = actions ?: {},
            )
        }
    }
}
