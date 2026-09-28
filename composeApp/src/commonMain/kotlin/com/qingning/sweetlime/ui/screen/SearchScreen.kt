package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.SearchEngine
import com.qingning.sweetlime.core.SearchHit
import com.qingning.sweetlime.ui.components.GlassTopBarScaffold
import com.qingning.sweetlime.ui.components.PressableRow
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.ui.components.TiltPressTextField
import com.qingning.sweetlime.ui.components.pageChunks
import com.qingning.sweetlime.ui.components.TiltPressCard

/**
 * 全局搜索（二级页）。
 *
 * **这一个框里的字，就是要转换的原文。** 它同时干两件事：
 * - 当**关键词**：去匹配样式名 / 分类名 / 英文 id、符号分类 / 符号本身、单字汉字；
 * - 当**原文**：样式结果的预览直接用它；点进详情页后，「原文」那一栏也还是它。
 *
 * 所以搜完不用回主页再敲一遍 —— 搜到的就是「这句话套上各种样式长什么样」。
 * 结果按「汉字信息 → 样式 → 符号」三类分段展示，每一段下面才是具体条目。
 * 搜索是纯内存的（[SearchEngine]），每敲一个字就重算，不做防抖也不会卡。
 */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenStyle: (styleId: String, input: String) -> Unit,
    onOpenGroup: (groupId: String) -> Unit,
    onOpenSymbols: (index: Int) -> Unit,
    onOpenTool: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    // 关键词与原文是同一份内容 —— 这就是「搜什么，转什么」。
    val hits = remember(q) { SearchEngine.search(q, q) }

    GlassTopBarScaffold(title = "搜索", onBack = onBack, modifier = modifier) { topPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = topPadding + 8.dp, bottom = 48.dp),
        ) {
            item(key = "search_field") {
                TiltPressTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = "输入文字，同时搜索样式 / 符号",
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
            }

            if (q.isEmpty()) {
                item(key = "search_hint") {
                    HintCard(
                        "输入的这行字既是搜索词，也是要转换的原文。\n" +
                            "可以搜：样式名（花体 / 圆圈 / 箭头）、英文名（circled）、" +
                            "分类名（特效符号 / 空白字符）、符号本身，或单个汉字查拼音拆分。",
                    )
                }
                return@LazyColumn
            }

            if (hits.isEmpty()) {
                item(key = "search_empty") {
                    HintCard("没有找到和「$q」相关的内容。")
                }
                return@LazyColumn
            }

            item(key = "search_count") {
                SmallTitle(text = "「$q」找到 ${hits.size} 条")
            }

            // 按类型分段渲染：每换一类，先插一个小标题，再插一张卡片装这一段的条目。
            var index = 0
            while (index < hits.size) {
                val label = hits[index].sectionLabel()
                var end = index
                while (end < hits.size && hits[end].sectionLabel() == label) end++
                val sectionEnd = end
                val sectionStart = index

                item(key = "section_$sectionStart") {
                    SmallTitle(text = "$label · ${sectionEnd - sectionStart}")
                }
                // 同一段里也可能很长：每 6 条一张卡片、卡片之间留空隙。
                items(
                    items = pageChunks(sectionEnd - sectionStart, per = 6),
                    key = { "card_${sectionStart}_${it.first}" },
                ) { range ->
                    TiltPressCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        for (offset in range) {
                            val i = sectionStart + offset
                            val hit = hits[i]
                            // 先取出样式（只有样式条目才有），避免在 lambda 里对 hit 做智能转换。
                            val style = hit as? SearchHit.Style
                            SearchRow(
                                hit = hit,
                                onOpenStyle = {
                                    style?.let { onOpenStyle(it.transform.id, q) }
                                },
                                onOpenGroup = onOpenGroup,
                                onOpenSymbols = onOpenSymbols,
                                onOpenTool = onOpenTool,
                                onCopyText = onCopyText,
                            )
                            if (offset != range.last) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
                index = sectionEnd
            }
        }
    }
}

/** 一类的标题（用于把结果分段）。 */
private fun SearchHit.sectionLabel(): String = when (this) {
    is SearchHit.Hanzi -> "汉字"
    is SearchHit.Style -> "样式"
    is SearchHit.StyleCategory -> "样式分类"
    is SearchHit.Symbol -> "符号"
    is SearchHit.SymbolCategory -> "符号分类"
    is SearchHit.Tool -> "工具"
}

@Composable
private fun SearchRow(
    hit: SearchHit,
    onOpenStyle: () -> Unit,
    onOpenGroup: (String) -> Unit,
    onOpenSymbols: (Int) -> Unit,
    onOpenTool: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
) {
    when (hit) {
        is SearchHit.Style -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = onOpenStyle,
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Basic.ArrowRight,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        is SearchHit.StyleCategory -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = { onOpenGroup(hit.groupId) },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Basic.ArrowRight,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        is SearchHit.Symbol -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = { onCopyText(hit.symbol, "符号") },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = "复制符号",
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        is SearchHit.SymbolCategory -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = { onOpenSymbols(hit.index) },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Basic.ArrowRight,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        is SearchHit.Tool -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = { onOpenTool(hit.id) },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Basic.ArrowRight,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )

        is SearchHit.Hanzi -> PressableRow(
            title = hit.title,
            summary = hit.summary,
            onClick = { onCopyText(hit.payload, "拼音 / 拆分") },
            endActions = {
                Icon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = "复制拼音",
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            },
        )
    }
}

@Composable
private fun HintCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 顶部要留出空隙：搜索框（尤其是带 Tilt 的输入框）和这张小字卡片
            // 原本是贴在一起的，看着像同一块。
            .padding(horizontal = 12.dp)
            .padding(top = 10.dp),
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(16.dp),
        )
    }
}
