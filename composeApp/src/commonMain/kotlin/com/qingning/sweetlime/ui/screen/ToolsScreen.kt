package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.lazy.rememberLazyListState
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.core.i18n.tr
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TOOL_ENTRIES
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 工具页（底层）：一列**竖排的工具入口**。
 *
 * 这里只做目录，点任意一条才 push 进对应的工具二级页 ——
 * 每个工具都有输入框、有结果、有说明，全部塞在一页里必然又长又乱。
 */
@Composable
fun ToolsScreen(
    outerPadding: PaddingValues,
    onOpenTool: (String) -> Unit,
) {
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
        item(key = "tool_title") {
        }
        // 工具按用途分四组：文字处理 / 编码转换 / 字符素材 / 实用工具。
        // 每组一行 miuix 灰色小字标题，下面一张卡。
        TOOL_SECTIONS.forEachIndexed { sectionIndex, (title, ids) ->
            item(key = "tool_section_$sectionIndex") {
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
                        ids.forEach { id ->
                            val tool = TOOL_ENTRIES.firstOrNull { it.id == id } ?: return@forEach
                            BasicComponent(
                                title = tool.title,
                                summary = tool.summary,
                                onClick = { onOpenTool(tool.id) },
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

/** 工具页分组：标题 + 工具 id。 */
private val TOOL_SECTIONS: List<Pair<String, List<String>>> = listOf(
    "文字处理" to listOf("pinyin", "chai", "daxie", "qqname"),
    "编码转换" to listOf("base64", "md5", "url", "radix", "color"),
    "字符素材" to listOf("symbol", "kaomoji"),
    "实用工具" to listOf("sniff", "currency", "bmi", "level", "editor"),
)
