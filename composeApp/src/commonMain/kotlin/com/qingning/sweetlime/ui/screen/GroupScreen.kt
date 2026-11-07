package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.mutableStateOf
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
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.PreviewEntry
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.data.FavoritesStore
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.core.listPreview

@Composable
fun GroupScreen(
    group: StyleGroup,
    input: String,
    favorites: FavoritesStore,
    onBack: () -> Unit,
    onCopy: (TransformItem) -> Unit,
    onToggleFavorite: (TransformItem) -> Unit,
    onOpenItem: (TransformItem) -> Unit,
    modifier: Modifier = Modifier,
) {

    val styles = remember(group) { TransformRegistry.all.filter { it.group == group } }
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {

        var rows by remember(group, input) { mutableStateOf<List<PreviewEntry?>?>(null) }

        var minElapsed by remember(group, input) { mutableStateOf(false) }
        LaunchedEffect(group, input) {
            delay(LOADING_MIN_MS)
            minElapsed = true
        }
        LaunchedEffect(group, input) {

            withFrameNanos { }

            val computed = withContext(Dispatchers.Default) {
                val out = ArrayList<PreviewEntry?>(styles.size)
                styles.forEachIndexed { index, transform ->
                    val item = TransformItem.of(transform, input)
                    out.add(if (item == null) null else PreviewEntry(item, item.output.listPreview()))
                    if (index % COMPUTE_CHUNK == COMPUTE_CHUNK - 1) delay(COMPUTE_YIELD_MS)
                }
                out
            }

            withFrameNanos { }
            rows = computed
        }
        if (input.isBlank() || styles.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(top = topBarHeight)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = tr("先在主页输入文字，再进来看这个分类的效果。"),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            return@Column
        }
        val prepared = rows
        if (prepared == null || !minElapsed) {
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
            return@Column
        }

        var contentVisible by remember(group, input) { mutableStateOf(false) }
        LaunchedEffect(group, input, prepared) {
            if (prepared != null) {
                withFrameNanos { }
                contentVisible = true
            }
        }
        val contentAlpha by animateFloatAsState(
            targetValue = if (contentVisible) 1f else 0f,
            animationSpec = tween(CONTENT_FADE_MS, easing = FastOutSlowInEasing),
            label = "groupContentAlpha",
        )
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = CONTENT_SLIDE_PX * (1f - contentAlpha)
                }
                .overScrollVertical()
,

            contentPadding = PaddingValues(top = topBarHeight, bottom = 48.dp),
        ) {
            item(key = "group_count") {
                SmallTitle(text = trf("共 {} 种样式 · 点条目看详情", styles.size))
            }

            val shown = prepared.filter { it?.item != null }
            itemsIndexed(shown, key = { _, entry -> entry!!.item!!.styleId }) { index, entry ->
                val item = entry!!.item!!
                run {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)

                            .clip(groupRowShape(index, shown.size)),
                        cornerRadius = 0.dp,
                    ) {
                        BasicComponent(
                            title = item.styleTitle,
                            summary = entry.preview,
                            onClick = { onOpenItem(item) },
                            endActions = {
                                IconButton(onClick = { onCopy(item) }) {
                                    Icon(
                                        imageVector = MiuixIcons.Copy,
                                        contentDescription = tr("复制"),
                                        tint = MiuixTheme.colorScheme.onBackground,
                                    )
                                }
                                IconButton(onClick = { onToggleFavorite(item) }) {
                                    Icon(
                                        imageVector = if (favorites.contains(item.key)) {
                                            MiuixIcons.FavoritesFill
                                        } else {
                                            MiuixIcons.Favorites
                                        },
                                        contentDescription = if (favorites.contains(item.key)) tr("取消收藏") else tr("收藏"),
                                        tint = MiuixTheme.colorScheme.onBackground,
                                    )
                                }
                            },
                        )
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
                title = group.label,
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


private const val LOADING_MIN_MS = 400L

private const val COMPUTE_CHUNK = 4

private const val COMPUTE_YIELD_MS = 2L

private const val CONTENT_FADE_MS = 300

private const val CONTENT_SLIDE_PX = 36f

private fun groupRowShape(index: Int, count: Int): Shape {

    val r = 16.dp
    return when {
        count <= 1 -> RoundedCornerShape(r)
        index == 0 -> RoundedCornerShape(topStart = r, topEnd = r)
        index == count - 1 -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        else -> RectangleShape
    }
}
