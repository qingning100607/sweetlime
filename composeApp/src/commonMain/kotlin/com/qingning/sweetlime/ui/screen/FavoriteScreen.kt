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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.size
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.withFrameNanos
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
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
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.ui.components.GlassEntriesPopup
import com.qingning.sweetlime.ui.components.TextInputDialog
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem

private const val GROUP_EXPAND_MS = 280
private const val FAVORITE_LOAD_MIN_MS = 300L
private const val FAVORITE_COMPUTE_CHUNK = 4
private const val FAVORITE_COMPUTE_YIELD_MS = 2L

@Composable
fun FavoriteScreen(
    favorites: FavoritesStore,
    outerPadding: PaddingValues,
    onCopy: (TransformItem) -> Unit,
    onOpen: (TransformItem) -> Unit,
) {

    val favoriteKeys = favorites.keys.toList()
    var showAddGroup by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupError by remember { mutableStateOf("") }
    val loadedRows = remember(favoriteKeys, AppLocale.current) {
        mutableStateMapOf<String, List<TransformItem>>()
    }
    val byGroup = favoriteKeys.groupBy { favorites.groupOf(it) }
    val sections: List<Pair<String, List<String>>> = buildList {
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
                        text = if (favoriteKeys.isEmpty()) {
                            tr("收藏")
                        } else {
                            trf("已收藏 {} 条", favoriteKeys.size)
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
        if (favoriteKeys.isEmpty() && favorites.groups.isEmpty()) {
            item(key = "favorite_empty") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
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
                                .padding(horizontal = 16.dp)
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
                                        FavoriteGroupRows(
                                            keys = list,
                                            group = group,
                                            cache = loadedRows,
                                            groups = favorites.groups,
                                            onCopy = onCopy,
                                            onOpen = onOpen,
                                            onRemove = { favorites.remove(it) },
                                            onMove = { key, target -> favorites.moveTo(key, target) },
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

    TextInputDialog(
        show = showAddGroup,
        title = tr("添加分组"),
        value = groupName,
        onValueChange = {
            groupName = it
            groupError = ""
        },
        label = tr("分组名称"),
        confirmText = tr("保存"),
        onConfirm = {
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
        onDismissRequest = { showAddGroup = false },
        errorText = groupError,
    )
}

@Composable
private fun FavoriteGroupRows(
    keys: List<String>,
    group: String,
    cache: MutableMap<String, List<TransformItem>>,
    groups: List<String>,
    onCopy: (TransformItem) -> Unit,
    onOpen: (TransformItem) -> Unit,
    onRemove: (String) -> Unit,
    onMove: (String, String) -> Unit,
) {
    val cached = cache[group]
    val animateIn = cached == null
    var rows by remember(keys, AppLocale.current) { mutableStateOf(cached) }
    LaunchedEffect(keys, AppLocale.current) {
        if (cache[group] != null) {
            rows = cache[group]
            return@LaunchedEffect
        }
        withFrameNanos { }
        val computed = withContext(Dispatchers.Default) { buildFavoriteItemsAsync(keys) }
        delay(FAVORITE_LOAD_MIN_MS)
        withFrameNanos { }
        cache[group] = computed
        rows = computed
    }
    val prepared = rows
    if (prepared == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
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
        return
    }
    prepared.forEach { item ->
        FavoriteRow(
            item = item,
            groups = groups,
            currentGroup = group,
            animateIn = animateIn,
            onCopy = { onCopy(item) },
            onOpen = { onOpen(item) },
            onRemove = { onRemove(item.key) },
            onMove = { target -> onMove(item.key, target) },
        )
    }
}

@Composable
private fun FavoriteRow(
    item: TransformItem,
    groups: List<String>,
    currentGroup: String,
    animateIn: Boolean = true,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
    onRemove: () -> Unit,
    onMove: (String) -> Unit,
) {
    var showMove by remember { mutableStateOf(false) }

    val appear = remember { Animatable(if (animateIn) 0f else 1f) }
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
        val moveOptions = buildList {
            add("")
            addAll(groups)
        }
        GlassEntriesPopup(
            show = showMove,
            entries = listOf(
                DropdownEntry(
                    items = moveOptions.map { group ->
                        DropdownItem(
                            text = if (group.isEmpty()) tr("未分组") else group,
                            selected = group == currentGroup,
                            onClick = { onMove(group) },
                        )
                    },
                ),
            ),
            onDismissRequest = { showMove = false },
        )
    }
}

private suspend fun buildFavoriteItemsAsync(keys: List<String>): List<TransformItem> {
    val out = ArrayList<TransformItem>(keys.size)
    keys.forEachIndexed { index, key ->
        val (styleId, input) = TransformItem.fromKey(key) ?: return@forEachIndexed
        val style = TransformRegistry.byId(styleId) ?: return@forEachIndexed
        out.add(TransformItem(styleId, style.title, input, style.transform(input)))
        if (index % FAVORITE_COMPUTE_CHUNK == FAVORITE_COMPUTE_CHUNK - 1) {
            delay(FAVORITE_COMPUTE_YIELD_MS)
        }
    }
    return out
}
