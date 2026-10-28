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
import com.qingning.sweetlime.ui.UpdateDownloadState
import com.qingning.sweetlime.ui.updateBannerText
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

/** 主页上的一个分类条目。 */
private class GroupEntry(
    val group: StyleGroup,
    val count: Int,
    val preview: String,
)

/** 昵称类平台的常见长度上限，超了就在字数旁边提醒一下。 */
private const val NICKNAME_SOFT_LIMIT = 24

/**
 * 主页：上面是输入框，下面是一列**竖排的分类条目**。
 *
 * 每个条目只显示「分类名 + 样式数 + 一条预览」，点进去才是完整的样式列表。
 * 所以主页没有横向滚动、也没有上百行内容，列表短、滑动轻、一眼能看完。
 *
 * 预览只取每个分类里第一条能生效的结果（按输入缓存），
 * 敲字时只做「分类数」次转换，而不是把全部 190+ 条样式都算一遍。
 *
 * 输入框下面补了一排小工具：字数统计 + 粘贴 + 一键清空；
 * 再往下（有历史时）是「最近使用」的一排胶囊，常用的样式一点就到位。
 *
 * 启动时查到了更新的版本，最顶上会多一条「发现新版本」提示（[newVersion] 非空时）：
 * 点整行就**在应用内直接下载安装包**（下好自动唤起系统安装器），点右边的「×」忽略这一版。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    input: String,
    onInputChange: (String) -> Unit,
    outerPadding: PaddingValues,
    onOpenGroup: (StyleGroup) -> Unit,
    recentIds: List<String> = emptyList(),
    onOpenRecent: (String) -> Unit = {},
    /** 查到的新版本号；null = 不显示顶部提示。 */
    newVersion: String? = null,
    /** 「下载 → 唤起安装器」的进度；和设置页共用同一份状态。 */
    updateDownload: UpdateDownloadState = UpdateDownloadState.Idle,
    /** 点提示整行：没下载就下载，下好了就打开系统安装器。 */
    onUpdateAction: () -> Unit = {},
    /** 点提示右边的「×」：这一版不再提示。 */
    onDismissUpdate: () -> Unit = {},
) {
    // 最近使用：收起时只排一行，右边的图标按钮点一下展开/收起。
    var recentExpanded by remember { mutableStateOf(false) }
    // 展开进度：0=收起、1=铺开。高度和箭头都跟着它走，展开时是「长出来」而不是跳一下。
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
    // 最近使用里已经失效的 id 直接跳过，顺便还原出标题。
    //
    // 键必须是 recentIds.toList()：RecentStore.ids 现在直接返回内部的
    // SnapshotStateList，而它的 equals 是**引用比较**（不是逐元素比较），
    // 实例一直是同一个，拿它当 remember 的键就永远不会失效，列表会变成死的。
    // toList() 换来一个普通 List：既按内容判等，读的过程也顺带订阅了列表变化。
    // 分组标题要过翻译，所以在这里取一次，下面直接遍历。
    val homeSections = HOME_SECTIONS
    val recentStyles = remember(recentIds.toList()) {
        recentIds.mapNotNull { id -> TransformRegistry.byId(id) }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),
        // 顶部 / 底部都给足空间：底部栏和右下角的悬浮设置按钮都不会压住最后一条。
        contentPadding = PaddingValues(
            top = outerPadding.calculateTopPadding(),
            bottom = outerPadding.calculateBottomPadding(),
        ),
    ) {
        // 顶部提示占一个「一直存在」的空位：查到更新就把它填成一条卡片，没查到就是空的。
        //
        // 这个空位不能省。更新检查是启动之后才回来的，如果那一刻才把这段 item 插到列表最前面，
        // LazyColumn 会把「当时的第一项」（输入框）按 key 钉在原地，于是整个列表被自动下推
        // 整整一条横幅的高度，横幅就被顶进顶栏、和「SweetLime」大标题叠在一起（实测反馈的那个问题）。
        // 位置固定成「一直存在」之后，横幅出现只是这一格长高，列表的滚动位置不会被挪动。
        item(key = "home_update_banner") {
            if (newVersion != null) {
                // 标题和说明跟着下载状态走：发现新版本 → 正在下载 42% → 点击打开系统安装器。
                val banner = updateBannerText(newVersion, updateDownload)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .padding(top = 8.dp),
                ) {
                    BasicComponent(
                        title = banner.title,
                        summary = banner.summary,
                        onClick = onUpdateAction,
                        endActions = {
                            // 只留一个「×」。以前这里还并排放了一个「>」箭头，
                            // 两个图标挤在一起会叠到横幅右边缘上，看着很别扭；
                            // 整行本来就可点，箭头是多余的，去掉。
                            IconButton(onClick = onDismissUpdate) {
                                Icon(
                                    imageVector = MiuixIcons.Clear,
                                    contentDescription = tr("这一版不再提示"),
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            }
                        },
                    )
                    // 下载失败时，小字下面再补一行「怎么办」：更新源在 GitHub，
                    // 国内直连经常抽风，与其让用户反复点「重试」不如直接给条明路。
                    if (banner.hint != null) {
                        Text(
                            text = banner.hint,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        )
                    }
                }
            }
        }
        item(key = "home_input") {
            // 主页这个输入框走 miuix 原生点击高亮（不再有「按角沉下去」的倾斜反馈）。
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
        // 输入框下面的一排小工具：字数 + 粘贴 + 清空。
        item(key = "home_input_tools") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 字数按**码点**算，不用 input.length：后者是 UTF-16 单元数，
                // 一个 emoji 会算成 2 个字（🎉🎉🎉 显示 6 字），平台限长提示就失真了。
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
        // 最近使用：有历史才显示，横向一排胶囊，点击直接进对应样式详情。
        if (recentStyles.isNotEmpty()) {
            item(key = "home_recent_title") {
                SmallTitle(text = tr("最近使用"))
            }
            item(key = "home_recent_list") {
                // 收起：铺满一行（放不下的裁掉）；展开：换行铺满所有记录。
                // 右边那个纯图标按钮（V 形箭头）切换，展开时箭头朝上。
                Box(modifier = Modifier.fillMaxWidth()) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            // 收起 / 展开切换 maxLines 时高度是跳变的，这里补一段高度动画。
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
        // 输入框和下面的样式分类之间留一点空隙，不然贴得太紧。
        item(key = "home_gap") {
            Spacer(modifier = Modifier.height(18.dp))
        }
        // 这里以前有一行小标题「样式分类 · 点一条进入」，
        // 它正好压在顶栏玻璃的渐隐带上，模糊之后显得很突兀，所以直接删掉。
        // 主页分两类：英文数字（花体/上下标/彩色字母）、中文（符号与装饰）。
        // 每类上面一行 miuix 灰色小字，下面一张卡。
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

/** 主页分类的分组：字母数字 / 符号与空白 / 昵称装饰。 */
private val HOME_SECTIONS: List<Pair<String, List<StyleGroup>>> = listOf(
    "字母与数字" to listOf(StyleGroup.LATIN, StyleGroup.SUPSUB, StyleGroup.COLOR),
    "符号与空白" to listOf(StyleGroup.OVERLAY, StyleGroup.SYMBOL, StyleGroup.BLANK),
    "昵称装饰" to listOf(
        StyleGroup.HUAYANG,
        StyleGroup.WING,
        StyleGroup.VINE,
        StyleGroup.BIRD,
    ),
)
