package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.PreviewCache
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.data.FavoritesStore
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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

/**
 * 某个分类的二级页：竖排列出该分类下的全部样式。
 *
 * 只有进到这一页才会把整组的样式都算出来（并按输入缓存），
 * 所以主页始终很轻。
 */
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
    // 只筛出这一组的样式对象，**不预先算输出**。
    // 以前这里是 mapNotNull { TransformItem.of(it, input) }：进页面就把整组（最多两百多套）
    // 的转换 + 预览全算一遍，全压在第一帧上，所以「神仙网名」这种大组会掉一下帧。
    // 现在转换挪到每一行自己的 remember 里，屏幕外的行不组合、也就不算。
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
        // 内容层：Haze 的采样源。顶部空出顶栏高度，滚动时卡片从顶栏下面穿过去并被实时糊。
        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {
        if (input.isBlank() || styles.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    // 空状态：自己让出顶栏高度（顶栏是浮层，会盖在上面）。
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
        val listState = rememberLazyListState()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .hyperScrollHaptic(),
            // 顶部让出顶栏高度 —— 写在 contentPadding 里（不是外层占位），
            // 这样滚动时条目能滑到顶栏**下面**被实时糊掉。
            contentPadding = PaddingValues(top = topBarHeight, bottom = 48.dp),
        ) {
            item(key = "group_count") {
                SmallTitle(text = trf("共 {} 种样式 · 点条目看详情", styles.size))
            }
            // 一行一个 LazyColumn item：只组合屏幕附近的几行。
            // 圆角只给首行的上边和末行的下边，中间行四角全平，拼成同一张卡的样子。
            itemsIndexed(styles, key = { _, style -> style.id }) { index, style ->
                val entry = remember(style, input, PreviewCache.version) { PreviewCache.entry(style, input) }
                val item = entry.item
                if (item != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            // miuix 的 Card 只收一个统一的 cornerRadius，做不出「只圆外侧两角」。
                            // 所以内层留平、外层按行位置裁形：首行圆上面、末行圆下面、中间直接拼平，
                            // 相邻两行边缘完全重合，竖着看就是一张连着的卡。
                            .clip(groupRowShape(index, styles.size)),
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
        // 顶栏浮层：实时模糊（Haze）+ 标题 + 返回。卡片从它下面穿过去时逐帧被糊。
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
                // 顶栏自己不铺底。
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

/** 一行在整张卡里的形状：首行只圆上面两角、末行只圆下面两角，中间行拼平；单行四角全圆。 */
private fun groupRowShape(index: Int, count: Int): Shape {
    // 16dp = miuix Card 的默认半径。
    val r = 16.dp
    return when {
        count <= 1 -> RoundedCornerShape(r)
        index == 0 -> RoundedCornerShape(topStart = r, topEnd = r)
        index == count - 1 -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        else -> RectangleShape
    }
}
