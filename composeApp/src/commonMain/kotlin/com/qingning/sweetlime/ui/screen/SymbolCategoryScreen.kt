package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import top.yukonga.miuix.kmp.utils.overScrollVertical
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import androidx.compose.runtime.remember
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 特殊符号的某个分类（三级页）：符号网格，点击即复制。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymbolCategoryScreen(
    index: Int,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeIndex = index.coerceIn(0, SYMBOL_CATEGORIES.lastIndex)
    val category = SYMBOL_CATEGORIES[safeIndex]
    // 关键：符号**按行切片**，每一行是一个独立的 lazy item。
    // 以前是「一个 item 里 forEach 全部符号」——等于一进页面就把整页几百个 chip 全组合出来，
    // 内容多的分类（特殊符号那种）必掉帧，LazyColumn 也白用了。
    val symbolRows = remember(category.symbols) { category.symbols.chunked(6) }
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {
        // 内容层 = Haze 的采样源。整页铺满，滚动时符号从顶栏**下面穿过去**并被实时糊掉
        // —— 这就是主页顶栏那种模糊。以前这里只铺了一层底，没东西可糊，所以看着没模糊。
            // 采样层单独一层、不参与滚动（与详情页/设置页同一套）。
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .pageBackdropLayer(),
            ) {
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .hyperScrollHaptic(),
            contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
        ) {
            item {
                Column {
                    // 顶栏高度写在「滚动内容」里（不是外层占位），内容才能滚到顶栏下面去。
                    Spacer(modifier = Modifier.height(topBarHeight))
                    Text(
                        text = trf("共 {} 个符号 · 点击即复制", category.symbols.size),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            // 每一行单独成一个 item：LazyColumn 只组合可见的那几行。
            items(count = symbolRows.size) { rowIndex ->
                // 以前用 FlowRow + 内容自适应宽度的 chip：每个符号宽度不一样，
                // 翅膀 / 表情这类宽字符会把后面的格子顶歪，整片看着就不齐。
                // 现在每行固定 6 格、每格等宽（weight(1f)），最后一行不满就补空位。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    symbolRows[rowIndex].forEach { symbol ->
                        SymbolChip(
                            symbol = symbol,
                            onClick = { onCopyText(symbol, category.title) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(symbolRows[rowIndex].size.let { 6 - it }) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
            }

        // 顶栏浮层：实时模糊（Haze）+ 标题 + 返回。符号从它下面穿过去时逐帧被糊掉。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bleedHorizontal()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 20.dp,
                            noiseFactor = 0.15f,
                            tint = HazeTint(hazeTint.copy(alpha = 0.30f)),
                        ),
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
            )
            // 兜底：过渡动画（预测性返回预览）时页面是被平移的，Haze 采到的是平移**之前**的画面，
            // 顶栏最上面那一条（状态栏那条）于是会把旧位置的文字透出来。
            // 这里在玻璃之上再蒙一层自上而下由浓变无的主题底色，把那一条压实；
            // 静止时下面本来就有模糊 + 主题色，观感几乎不变。
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to MiuixTheme.colorScheme.surface.copy(alpha = 0.55f),
                            0.6f to Color.Transparent,
                        ),
                    ),
            )
            SmallTopAppBar(
                // 顶栏自己不铺底：二级页的背景已经是「实流光」了，铺底会把顶部那块盖成纯白。
                color = Color.Transparent,
                title = category.title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = tr("返回"),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun SymbolChip(symbol: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            // 等宽 + 固定高：不同宽度的符号（翅膀、表情）也排在一条竖线上。
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            maxLines = 1,
        )
    }
}