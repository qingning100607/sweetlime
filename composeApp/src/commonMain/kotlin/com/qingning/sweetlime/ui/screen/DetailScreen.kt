package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.readClipboard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.core.codePointLength

@Composable
fun DetailScreen(
    transform: TextTransform,
    initialInput: String,
    isFavorite: (String) -> Boolean,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {

    var text by rememberSaveable(transform.id) { mutableStateOf(initialInput) }

    var output by remember(transform.id) { mutableStateOf<String?>(null) }

    var minElapsed by remember(transform.id) { mutableStateOf(false) }
    LaunchedEffect(transform.id) {
        delay(LOADING_MIN_MS)
        minElapsed = true
    }
    LaunchedEffect(transform.id, text) {

        withFrameNanos { }
        val computed = withContext(Dispatchers.Default) { transform.transform(text) }

        withFrameNanos { }
        output = computed
    }
    val contentReady = output != null
    val resolved = output.orEmpty()
    val hasEffect = contentReady && resolved != text && text.isNotEmpty()

    val effectText = when {
        text.isEmpty() -> tr("上面输入文字，这里就是转换后的效果。")
        hasEffect -> resolved
        else -> tr("这段文字在这个样式里没有变化。")
    }
    val effectChunks = remember(effectText) { splitForDisplay(effectText) }

    val favorite = remember(text, transform.id) { isFavorite(text) }

    val showSpinner = !contentReady || !minElapsed
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
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

        var contentVisible by remember(transform.id) { mutableStateOf(false) }
        LaunchedEffect(transform.id, contentReady, minElapsed) {
            if (contentReady && minElapsed) {
                withFrameNanos { }
                contentVisible = true
            }
        }
        val contentAlpha by animateFloatAsState(
            targetValue = if (contentVisible) 1f else 0f,
            animationSpec = tween(CONTENT_FADE_MS, easing = FastOutSlowInEasing),
            label = "detailContentAlpha",
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = CONTENT_SLIDE_PX * (1f - contentAlpha)
                }
                .overScrollVertical()
                .hyperScrollHaptic(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column {

            Spacer(modifier = Modifier.height(topBarHeight))

SmallTitle(text = tr("原文"))
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onInputChange(it)
                    },
                    label = tr("输入要转换的文字"),
                    useLabelAsPlaceholder = true,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = trf("{} 字", text.codePointLength()),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            readClipboard()?.let {
                                text = it
                                onInputChange(it)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Paste,
                            contentDescription = tr("粘贴"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                text = ""
                                onInputChange("")
                            },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Clear,
                                contentDescription = tr("清空"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                }
            }

        }
        }

        item {
            SmallTitle(text = tr("效果"))
        }

        if (hasEffect && effectChunks.size > 1) {
            itemsIndexed(effectChunks) { index, chunk ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(effectChunkShape(index, effectChunks.size)),
                    cornerRadius = 0.dp,
                ) {
                    Text(
                        text = chunk,
                        style = MiuixTheme.textStyles.title2,
                        color = MiuixTheme.colorScheme.onSurfaceContainer,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = if (index == 0) 16.dp else 0.dp,
                            bottom = if (index == effectChunks.size - 1) 16.dp else 0.dp,
                        ),
                    )
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    Text(
                        text = effectText,
                        style = MiuixTheme.textStyles.title2,
                        color = if (hasEffect) {
                            MiuixTheme.colorScheme.onSurfaceContainer
                        } else {
                            MiuixTheme.colorScheme.onSurfaceContainerVariant
                        },
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        item {
            Column {

            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = trf("{} 字", resolved.codePointLength()),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = tr("复制"),
                    onClick = { onCopy(resolved) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
                TextButton(
                    text = tr("分享"),
                    onClick = { onShare(resolved) },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = if (favorite) tr("取消收藏") else tr("收藏"),
                    onClick = { onToggleFavorite(text) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

                }
            }
        }
        }
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
                title = transform.title,
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

private val DETAIL_LOADING_SIZE = 44.dp

private const val LOADING_MIN_MS = 400L

private const val CONTENT_FADE_MS = 300

private const val CONTENT_SLIDE_PX = 36f

private const val EFFECT_CHUNK_SIZE = 800

private fun splitForDisplay(source: String): List<String> {
    if (source.length <= EFFECT_CHUNK_SIZE) return listOf(source)
    val out = ArrayList<String>(source.length / EFFECT_CHUNK_SIZE + 1)
    var start = 0
    while (start < source.length) {
        var end = minOf(start + EFFECT_CHUNK_SIZE, source.length)
        if (end < source.length && source[end].isLowSurrogate()) end += 1
        val newline = source.lastIndexOf('\n', end - 1)
        if (newline > start + EFFECT_CHUNK_SIZE / 3) end = newline + 1
        out.add(source.substring(start, end))
        start = end
    }
    return out
}

private fun effectChunkShape(index: Int, count: Int): Shape {
    val r = 16.dp
    return when {
        count <= 1 -> RoundedCornerShape(r)
        index == 0 -> RoundedCornerShape(topStart = r, topEnd = r)
        index == count - 1 -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        else -> RectangleShape
    }
}
