package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.PreviewCache
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.readClipboard
import com.qingning.sweetlime.ui.components.ChipButton
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.core.codePointLength
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import com.qingning.sweetlime.core.listPreview
import top.yukonga.miuix.kmp.basic.TextField

private class GroupEntry(
    val group: StyleGroup,
    val count: Int,
    val preview: String,
)

private const val NICKNAME_SOFT_LIMIT = 24

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    input: String,
    onInputChange: (String) -> Unit,
    outerPadding: PaddingValues,
    onOpenGroup: (StyleGroup) -> Unit,
    recentIds: List<String> = emptyList(),
    onOpenRecent: (String) -> Unit = {},
    newVersion: String? = null,
    onOpenUpdateDialog: () -> Unit = {},
) {

    var recentExpanded by remember { mutableStateOf(false) }

    val recentExpandProgress by animateFloatAsState(
        targetValue = if (recentExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
        label = "recentExpand",
    )
    val entries = remember(input) {
        TransformRegistry.grouped.map { (group, styles) ->
            val preview = if (input.isBlank()) {
                ""
            } else {
                styles.firstNotNullOfOrNull { PreviewCache.entry(it, input).item?.output }.orEmpty()
            }
            GroupEntry(group, styles.size, preview)
        }
    }

    val homeSections = HOME_SECTIONS
    val recentStyles = remember(recentIds.toList()) {
        recentIds.mapNotNull { id -> TransformRegistry.byId(id) }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),

        contentPadding = PaddingValues(
            top = outerPadding.calculateTopPadding(),
            bottom = outerPadding.calculateBottomPadding(),
        ),
    ) {
        item(key = "home_update_banner") {
            if (newVersion != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp),
                ) {
                    BasicComponent(
                        title = trf("发现新版本 {}", newVersion),
                        summary = tr("点击查看更新内容"),
                        onClick = onOpenUpdateDialog,
                    )
                }
            }
        }
        item(key = "home_input") {

            TextField(
                value = input,
                onValueChange = onInputChange,
                label = tr("输入要转换的文字"),
                useLabelAsPlaceholder = true,
                maxLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp),
            )
        }

        item(key = "home_input_tools") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {

                val charCount = remember(input) { input.codePointLength() }
                Text(
                    text = if (charCount > NICKNAME_SOFT_LIMIT) {
                        trf("{} 字 · 偏长，部分平台会截断", charCount)
                    } else {
                        trf("{} 字", charCount)
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { readClipboard()?.let(onInputChange) }) {
                    Icon(
                        imageVector = MiuixIcons.Paste,
                        contentDescription = tr("粘贴"),
                        tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
                if (input.isNotEmpty()) {
                    IconButton(onClick = { onInputChange("") }) {
                        Icon(
                            imageVector = MiuixIcons.Clear,
                            contentDescription = tr("清空"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                }
            }
        }

        if (recentStyles.isNotEmpty()) {
            item(key = "home_recent_title") {
                SmallTitle(text = tr("最近使用"))
            }
            item(key = "home_recent_list") {

                Box(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()

                            .animateContentSize(
                                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing),
                            )
                            .padding(start = 12.dp, end = 56.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxLines = if (recentExpanded) Int.MAX_VALUE else 1,
                    ) {
                        for (style in recentStyles) {
                            ChipButton(
                                text = style.title,
                                onClick = { onOpenRecent(style.id) },
                            )
                        }
                    }
                    if (recentStyles.size > 3) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(end = 6.dp),
                        ) {
                            IconButton(onClick = { recentExpanded = !recentExpanded }) {
                                Icon(
                                    imageVector = MiuixIcons.ChevronForward,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                    modifier = Modifier.rotate(90f + 180f * recentExpandProgress),
                                )
                            }
                        }
                    }
                }
            }
        }

        item(key = "home_gap") {
            Spacer(modifier = Modifier.height(18.dp))
        }

        homeSections.forEachIndexed { index, (title, groups) ->
            item(key = "home_section_$index") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        SmallTitle(text = tr(title))
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 12.dp),
                    ) {
                        groups.forEach { group ->
                            val entry = entries.firstOrNull { it.group == group } ?: return@forEach
                            BasicComponent(
                                title = entry.group.label,
                                summary = when {
                                    input.isBlank() -> trf("{} 种样式", entry.count)
                                    entry.preview.isNotEmpty() -> entry.preview.listPreview()
                                    else -> tr("这段文字在这里没有效果")
                                },
                                onClick = { onOpenGroup(entry.group) },
                                endActions = {
                                    Icon(
                                        imageVector = MiuixIcons.Basic.ArrowRight,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                    )
                                },
                            )
                        }
                    }
                }
            }
        }

    }
}

private val HOME_SECTIONS: List<Pair<String, List<StyleGroup>>> = listOf(
    "字母与数字" to listOf(StyleGroup.LATIN, StyleGroup.SUPSUB, StyleGroup.COLOR),
    "符号与空白" to listOf(StyleGroup.OVERLAY, StyleGroup.SYMBOL, StyleGroup.BLANK),
    "昵称装饰" to listOf(
        StyleGroup.HUAYANG,
        StyleGroup.WING,
        StyleGroup.VINE,
    ),
)
