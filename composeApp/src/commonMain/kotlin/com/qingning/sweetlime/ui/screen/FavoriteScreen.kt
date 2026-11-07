package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.rotate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.i18n.AppLocale
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.listPreview
import com.qingning.sweetlime.data.FavoritesStore
import com.qingning.sweetlime.ui.effect.rememberHyperHapticFeedback
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val GROUP_EXPAND_MS = 280

@Composable
fun FavoriteScreen(
    favorites: FavoritesStore,
    outerPadding: PaddingValues,
    onCopy: (TransformItem) -> Unit,
    onOpen: (TransformItem) -> Unit,
) {

    val items: List<TransformItem> = remember(favorites.keys.toList(), AppLocale.current) {
        favorites.keys.mapNotNull { key ->
            val (styleId, input) = TransformItem.fromKey(key) ?: return@mapNotNull null
            val style = TransformRegistry.byId(styleId) ?: return@mapNotNull null
            TransformItem(styleId, style.title, input, style.transform(input))
        }
    }
    var showAddGroup by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }

    var groupError by remember { mutableStateOf("") }

    val byGroup = items.groupBy { favorites.groupOf(it.key) }
    val sections: List<Pair<String, List<TransformItem>>> = buildList {
        val ungrouped = byGroup[""].orEmpty()
        if (ungrouped.isNotEmpty()) add("" to ungrouped)
        favorites.groups.sortedWith(String.CASE_INSENSITIVE_ORDER)
            .forEach { group -> add(group to byGroup[group].orEmpty()) }
    }

    val listState = rememberLazyListState()
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),
        contentPadding = PaddingValues(
            top = outerPadding.calculateTopPadding(),
            bottom = outerPadding.calculateBottomPadding(),
        ),
    ) {

        item(key = "favorite_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SmallTitle(
                        text = if (items.isEmpty()) {
                            tr("收藏")
                        } else {
                            trf("已收藏 {} 条", items.size)
                        },
                    )
                }
                IconButton(
                    onClick = {
                        groupName = ""
                        groupError = ""
                        showAddGroup = true
                    },
                    modifier = Modifier.padding(end = 26.dp),
                ) {
                    Icon(
                        imageVector = MiuixIcons.Add,
                        contentDescription = tr("添加分组"),
                        tint = MiuixTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        if (items.isEmpty() && favorites.groups.isEmpty()) {
            item(key = "favorite_empty") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        Text(
                            text = tr("还没有收藏。在分类页点条目右侧的心形按钮即可收藏。"),
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        } else {
            sections.forEach { (group, list) ->
                item(key = "favorite_section_$group") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()

                            .animateItem(
                                fadeInSpec = tween(200),
                                fadeOutSpec = tween(180),

                                placementSpec = null,
                            ),
                    ) {
                        val collapsed = favorites.isCollapsed(group)
                        val haptics = rememberHyperHapticFeedback()
                        val arrowTurn by animateFloatAsState(
                            targetValue = if (collapsed) -90f else 0f,
                            animationSpec = tween(GROUP_EXPAND_MS, easing = FastOutSlowInEasing),
                            label = "groupArrow",
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp)

                                .animateContentSize(
                                    animationSpec = tween(GROUP_EXPAND_MS, easing = FastOutSlowInEasing),
                                ),
                        ) {

                            val canDelete = group.isNotEmpty()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            favorites.toggleCollapsed(group)
                                        },
                                        onLongClick = if (!canDelete) {
                                            null
                                        } else {
                                            {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                favorites.removeGroup(group)
                                            }
                                        },
                                    ),
                            ) {
                                BasicComponent(
                                    title = if (group.isEmpty()) tr("未分组") else group,
                                    summary = trf("已收藏 {} 条", list.size),
                                    endActions = {
                                        Icon(
                                            imageVector = MiuixIcons.ExpandMore,
                                            contentDescription = if (collapsed) tr("展开") else tr("收起"),
                                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                            modifier = Modifier
                                                .padding(end = 16.dp)
                                                .size(22.dp)
                                                .rotate(arrowTurn),
                                        )
                                    },
                                )
                            }

                            if (!collapsed) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    if (list.isEmpty()) {
                                        Text(
                                            text = tr("这个分组还没有收藏，长按分组名可删除"),
                                            style = MiuixTheme.textStyles.body2,
                                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                            modifier = Modifier.padding(16.dp),
                                        )
                                    } else {
                                        list.forEach { item ->
                                            FavoriteRow(
                                                item = item,
                                                groups = favorites.groups,
                                                currentGroup = group,
                                                onCopy = { onCopy(item) },
                                                onOpen = { onOpen(item) },
                                                onRemove = { favorites.remove(item.key) },
                                                onMove = { target -> favorites.moveTo(item.key, target) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    WindowBottomSheet(
        show = showAddGroup,
        onDismissRequest = { showAddGroup = false },
        title = tr("添加分组"),
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            TextField(
                value = groupName,
                onValueChange = {
                    groupName = it
                    groupError = ""
                },
                label = tr("分组名称"),
                useLabelAsPlaceholder = true,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (groupError.isNotEmpty()) {
                Text(
                    text = groupError,
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = tr("取消"),
                    onClick = { showAddGroup = false },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = tr("保存"),
                    onClick = {
                        val name = groupName.trim()
                        groupError = when {
                            name.isEmpty() -> tr("分组名称不能为空")
                            favorites.groups.any { it.equals(name, ignoreCase = true) } ->
                                trf("已有同名分组「{}」", name)
                            favorites.addGroup(name) -> ""
                            else -> tr("分组名称不能为空")
                        }
                        if (groupError.isEmpty()) showAddGroup = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FavoriteRow(
    item: TransformItem,
    groups: List<String>,
    currentGroup: String,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onMove: (String) -> Unit,
) {
    var showMove by remember { mutableStateOf(false) }

    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        appear.animateTo(1f, tween(200, easing = FastOutSlowInEasing))
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 6.dp.toPx()
            },
    ) {
        BasicComponent(
            title = item.styleTitle,
            summary = item.output.listPreview(),
            onClick = onOpen,
            endActions = {
                IconButton(onClick = onCopy) {
                    Icon(
                        imageVector = MiuixIcons.Copy,
                        contentDescription = tr("复制"),
                        tint = MiuixTheme.colorScheme.onBackground,
                    )
                }
                IconButton(onClick = { showMove = true }) {
                    Icon(
                        imageVector = MiuixIcons.Basic.ArrowUpDown,
                        contentDescription = tr("移到分组"),
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
        OverlayListPopup(
            show = showMove,
            alignment = PopupPositionProvider.Align.End,
            onDismissRequest = { showMove = false },
        ) {
            ListPopupColumn {

                val options = buildList {
                    add("")
                    addAll(groups)
                }
                options.forEachIndexed { index, group ->
                    DropdownImpl(
                        text = if (group.isEmpty()) tr("未分组") else group,
                        optionSize = options.size,
                        isSelected = group == currentGroup,
                        index = index,
                        onSelectedIndexChange = {
                            onMove(group)
                            showMove = false
                        },
                    )
                }
            }
        }
    }
}
