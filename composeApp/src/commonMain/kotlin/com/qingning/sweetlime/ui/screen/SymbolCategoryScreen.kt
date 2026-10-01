package com.qingning.sweetlime.ui.screen

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
import com.qingning.sweetlime.ui.effect.flowingPageLayer
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

/** 特殊符号的某个分类（三级页）：符号网格，点一下即复制。 */
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
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 采样源：在顶栏这一条里铺一层和整页一模一样的底（流光 / 纯色），
            // 顶栏的模糊就糊它 —— 和主页顶栏同一套参数，观感一致。
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pageBackdropLayer()
                    .hazeSource(state = hazeState),
            ) {}
            Box(
                modifier = Modifier
                    .matchParentSize()
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
        Text(
            text = trf("共 {} 个符号 · 点一下即复制", category.symbols.size),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                category.symbols.forEach { symbol ->
                    SymbolChip(symbol = symbol) { onCopyText(symbol, category.title) }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SymbolChip(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = symbol,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )
    }
}