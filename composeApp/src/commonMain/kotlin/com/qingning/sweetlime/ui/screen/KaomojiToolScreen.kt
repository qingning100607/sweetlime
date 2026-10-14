package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.mapping.KAOMOJI_CATEGORIES
import com.qingning.sweetlime.ui.effect.SpringColumn
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 颜文字库：上面一行分类（横向滚动），下面把这个分类的颜文字列出来，点任意一条即复制。
 *
 * 数据在 `core/mapping/KaomojiData.kt`，14 个分类、146 条。
 * 分类名走词表（简 / 繁 / 英），所以这里只管展示，不写死语言。
 *
 * 外层用 [SpringColumn] 而不是裸 `LazyColumn` —— 这页内容通常不足一屏，
 * 裸着放橡皮筋会把整页内容往下拖、上面露出一大片页面底色（看着像被白层盖住）。
 */
@Composable
internal fun KaomojiToolScreen(onCopyText: (String, String) -> Unit) {
    var selected by rememberSaveable { mutableStateOf(0) }
    val category = KAOMOJI_CATEGORIES[selected.coerceIn(0, KAOMOJI_CATEGORIES.lastIndex)]

    SpringColumn {
        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))

        // 分类：横向滚动的一行小胶囊，选中的用高亮底色。
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
        ) {
            items(KAOMOJI_CATEGORIES.size) { index ->
                CategoryChip(
                    text = KAOMOJI_CATEGORIES[index].title,
                    selected = index == selected,
                    onClick = { selected = index },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = trf("颜文字 · 共 {} 条", category.items.size.toString()),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )

        // 一个分类一张卡：点一行即复制。
        Card(modifier = Modifier.fillMaxWidth()) {
            category.items.forEach { kaomoji ->
                BasicComponent(
                    title = kaomoji,
                    onClick = { onCopyText(kaomoji, tr("颜文字")) },
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Copy,
                            contentDescription = tr("复制"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
            }
        }
    }
}

/** 分类小胶囊：点一下换分类。 */
@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = MiuixTheme.colorScheme
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) scheme.primary else scheme.surfaceContainerHigh,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body1,
            color = if (selected) scheme.onPrimary else scheme.onSurfaceContainerVariant,
        )
    }
}