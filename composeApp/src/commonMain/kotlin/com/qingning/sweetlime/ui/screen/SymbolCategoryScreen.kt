package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer

private val chipWidthCache = HashMap<String, androidx.compose.ui.unit.Dp>()

private const val CHIP_WIDTH_SAMPLE = 24

private var cachedTopBarHeight = androidx.compose.ui.unit.Dp.Unspecified

private val SymbolChipShape = RoundedCornerShape(10.dp)

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

    val measurer = rememberTextMeasurer()
    val chipTextStyle = MiuixTheme.textStyles.body1
    val glyphDensity = LocalDensity.current
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val chipWidth = remember(category, chipTextStyle, glyphDensity) {
        val key = category.title + "|" + glyphDensity.density + "|" + glyphDensity.fontScale + "|" + chipTextStyle.fontSize
        chipWidthCache.getOrPut(key) {
            val symbols = category.symbols
            val stride = if (symbols.size <= CHIP_WIDTH_SAMPLE) 1 else symbols.size / CHIP_WIDTH_SAMPLE
            var widest = 0.dp
            var i = 0
            while (i < symbols.size) {
                val w = with(glyphDensity) {
                    measurer.measure(AnnotatedString(symbols[i]), chipTextStyle).size.width.toDp()
                }
                if (w > widest) widest = w
                i += stride
            }
            (widest + 32.dp).coerceIn(48.dp, 112.dp)
        }
    }

    val availableWidth = screenWidth - 24.dp
    val perRow = (((availableWidth + 8.dp) / (chipWidth + 8.dp)).toInt()).coerceIn(2, 8)
    val symbolRows = remember(category.symbols, perRow) { category.symbols.chunked(perRow) }
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    var topBarHeight by remember {
        mutableStateOf(
            if (cachedTopBarHeight == androidx.compose.ui.unit.Dp.Unspecified) {
                0.dp
            } else {
                cachedTopBarHeight
            },
        )
    }
    val density = LocalDensity.current

    var minElapsed by remember(safeIndex) { mutableStateOf(false) }
    LaunchedEffect(safeIndex) {
        delay(LOADING_MIN_MS)
        minElapsed = true
    }
    val showSpinner = !minElapsed
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .pageBackdropLayer(),
            ) {
            if (showSpinner) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = topBarHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        InfiniteProgressIndicator()
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = tr("载入中"),
                            style = MiuixTheme.textStyles.footnote1,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                }
            } else {

            var contentVisible by remember(safeIndex) { mutableStateOf(false) }
            LaunchedEffect(safeIndex) {
                withFrameNanos { }
                contentVisible = true
            }
            val contentAlpha by animateFloatAsState(
                targetValue = if (contentVisible) 1f else 0f,
                animationSpec = tween(CONTENT_FADE_MS, easing = FastOutSlowInEasing),
                label = "symbolContentAlpha",
            )
            LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = CONTENT_SLIDE_PX * (1f - contentAlpha)
                }
                .overScrollVertical()
,
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column {

                    Spacer(modifier = Modifier.height(topBarHeight))
                    Text(
                        text = trf("共 {} 个符号 · 点击即复制", category.symbols.size),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            items(count = symbolRows.size, key = { it }, contentType = { "symbol_row" }) { rowIndex ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    symbolRows[rowIndex].forEach { symbol ->
                        SymbolChip(
                            symbol = symbol,
                            onClick = { onCopyText(symbol, category.title) },
                            width = chipWidth,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
            }
            }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged {
                    val measured = with(density) { it.height.toDp() }
                    topBarHeight = measured
                    cachedTopBarHeight = measured
                },
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
private fun SymbolChip(symbol: String, onClick: () -> Unit, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier

            .width(width)
            .height(44.dp)
            .clip(SymbolChipShape)
            .background(MiuixTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            maxLines = 1,

            softWrap = false,
        )
    }
}

private const val LOADING_MIN_MS = 400L

private const val CONTENT_FADE_MS = 300

private const val CONTENT_SLIDE_PX = 36f
