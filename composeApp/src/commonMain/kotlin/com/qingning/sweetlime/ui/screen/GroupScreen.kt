package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.ui.effect.flowingPageLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.data.FavoritesStore
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
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
import com.qingning.sweetlime.ui.components.TiltPressCard
import com.qingning.sweetlime.ui.components.PressableRow
import com.qingning.sweetlime.ui.components.pageChunks

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
    val items = remember(input, group) {
        if (input.isBlank()) {
            emptyList()
        } else {
            TransformRegistry.all
                .filter { it.group == group }
                .mapNotNull { TransformItem.of(it, input) }
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        // 顶栏这一条也铺一层同样的流光：顶栏自己不铺底，铺的就是整页那层实流光。
        Box(modifier = Modifier.fillMaxWidth().flowingPageLayer()) {
        SmallTopAppBar(
            // 顶栏自己不铺底：二级页的背景已经是「实流光」了，铺底会把顶部那块盖成纯白。
            color = Color.Transparent,
            title = group.label,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = MiuixIcons.Back,
                        contentDescription = "返回",
                        tint = MiuixTheme.colorScheme.onBackground,
                    )
                }
            },
        )
        }
        if (items.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "先在主页输入文字，再进来看这个分类的效果。",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
            return@Column
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp),
        ) {
            item(key = "group_count") {
                SmallTitle(text = "共 ${items.size} 种样式 · 点条目看详情")
            }
            // 长目录切块：每 6 条一张卡片、卡片之间留空隙。
            // 整张巨大卡片一起倾倒太突兀，切小块后只有手指那一片会动，稳得多。
            items(
                items = pageChunks(items.size, per = 6),
                key = { "group_chunk_${it.first}" },
            ) { range ->
                TiltPressCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    for (index in range) {
                        val item = items[index]
                        PressableRow(
                            title = item.styleTitle,
                            summary = item.output.replace('\n', ' '),
                            onClick = { onOpenItem(item) },
                            endActions = {
                                IconButton(onClick = { onCopy(item) }) {
                                    Icon(
                                        imageVector = MiuixIcons.Copy,
                                        contentDescription = "复制",
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
                                        contentDescription = if (favorites.contains(item.key)) "取消收藏" else "收藏",
                                        tint = MiuixTheme.colorScheme.onBackground,
                                    )
                                }
                            },
                        )
                        if (index != range.last) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}