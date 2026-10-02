package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.i18n.AppLocale
import com.qingning.sweetlime.data.FavoritesStore
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/** 收藏页（底层）：竖排的收藏条目，点条目进详情。 */
@Composable
fun FavoriteScreen(
    favorites: FavoritesStore,
    outerPadding: PaddingValues,
    onCopy: (TransformItem) -> Unit,
    onOpen: (TransformItem) -> Unit,
) {
    // 收藏以 key 持久化，展示时按样式重算输出。
    //
    // 这里必须包 remember：重算要按 id 查注册表、还要对每条重跑一次 transform()，
    // 而收藏页任何一次重组（滚动、Snackbar、切语言）都会走到这一行。
    // 键里带上语言：样式标题是读时翻译的，不跟着语言重算的话标题会停留旧语言。
    val items: List<TransformItem> = remember(favorites.keys.toList(), AppLocale.current) {
        favorites.keys.mapNotNull { key ->
            val (styleId, input) = TransformItem.fromKey(key) ?: return@mapNotNull null
            val style = TransformRegistry.byId(styleId) ?: return@mapNotNull null
            TransformItem(styleId, style.title, input, style.transform(input))
        }
    }
    if (items.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 挂上滚动 + miuix 弹性 overscroll：内容不满一屏也能拖着弹。
                .verticalScroll(rememberScrollState())
                .overScrollVertical()
                .padding(top = outerPadding.calculateTopPadding()),
        ) {
            SmallTitle(text = tr("收藏"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    text = tr("还没有收藏。在分类页点条目右侧的心形按钮即可收藏。"),
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize().overScrollVertical()
            // miuix 的弹性 overscroll：滑到底还能继续拖，松手弹回。
            .overScrollVertical(),
        contentPadding = PaddingValues(
            top = outerPadding.calculateTopPadding(),
            bottom = outerPadding.calculateBottomPadding(),
        ),
    ) {
        item(key = "favorite_title") {
            SmallTitle(text = trf("已收藏 {} 条", items.size))
        }
        item(key = "favorite_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                items.forEachIndexed { index, item ->
                    FavoriteRow(
                        item = item,
                        onCopy = { onCopy(item) },
                        onOpen = { onOpen(item) },
                        onRemove = { favorites.remove(item.key) },
                    )
                    if (index != items.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    item: TransformItem,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
) {
    BasicComponent(
        title = item.styleTitle,
        summary = item.output.replace('\n', ' '),
        onClick = onOpen,
        endActions = {
            IconButton(onClick = onCopy) {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = tr("复制"),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = MiuixIcons.Delete,
                    contentDescription = tr("移除"),
                    tint = MiuixTheme.colorScheme.onBackground,
                )
            }
        },
    )
}