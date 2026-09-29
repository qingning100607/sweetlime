package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TransformItem
import com.qingning.sweetlime.core.TransformRegistry
import com.qingning.sweetlime.core.readClipboard
import com.qingning.sweetlime.ui.UpdateDownloadState
import com.qingning.sweetlime.ui.updateBannerText
import com.qingning.sweetlime.ui.components.ChipButton
import com.qingning.sweetlime.ui.components.TiltPressTextField
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    val entries = remember(input) {
        TransformRegistry.grouped.map { (group, styles) ->
            val preview = if (input.isBlank()) {
                ""
            } else {
                styles.firstNotNullOfOrNull { TransformItem.of(it, input)?.output }.orEmpty()
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
    val recentStyles = remember(recentIds.toList()) {
        recentIds.mapNotNull { id -> TransformRegistry.byId(id) }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
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
                // 标题和说明跟着下载状态走：发现新版本 → 正在下载 42% → 点一下打开系统安装器。
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
            // 主页这个输入框（用户口中的「主页搜索框」）保留按角沉下去的手感，
            // 跟搜索页的搜索框一致；其余地方都是 miuix 原生高亮。
            TiltPressTextField(
                value = input,
                onValueChange = onInputChange,
                label = tr("输入要转换的文字"),
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
                Text(
                    text = if (input.length > NICKNAME_SOFT_LIMIT) {
                        trf("{} 字 · 偏长，部分平台会截断", input.length)
                    } else {
                        trf("{} 字", input.length)
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
        // 最近使用：有历史才显示，横向一排胶囊，点一下直接进对应样式详情。
        if (recentStyles.isNotEmpty()) {
            item(key = "home_recent_title") {
                SmallTitle(text = tr("最近使用"))
            }
            item(key = "home_recent_list") {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(recentStyles.size) { index ->
                        val style = recentStyles[index]
                        ChipButton(
                            text = style.title,
                            onClick = { onOpenRecent(style.id) },
                        )
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
        item(key = "home_group_list") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                entries.forEachIndexed { index, entry ->
                    BasicComponent(
                        title = entry.group.label,
                        summary = when {
                            input.isBlank() -> trf("{} 种样式", entry.count)
                            entry.preview.isNotEmpty() -> entry.preview.replace('\n', ' ')
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
                    if (index != entries.lastIndex) {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

    }
}