package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.ui.effect.flowingPageLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TOOL_ENTRIES
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES
import com.qingning.sweetlime.core.tools.DaxieConverter
import com.qingning.sweetlime.core.tools.hanziToChai
import com.qingning.sweetlime.core.tools.hanziToPinyin
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.ui.components.PressableRow
import com.qingning.sweetlime.ui.components.TiltPressCard
import com.qingning.sweetlime.ui.components.TiltPressTextField
import com.qingning.sweetlime.ui.components.pageChunks

internal fun toolTitle(id: String): String =
    TOOL_ENTRIES.firstOrNull { it.id == id }?.title ?: "工具"

/** 单个工具的二级页：目录见 [TOOL_ENTRIES]，每个 id 对应一个页面。 */
@Composable
fun ToolScreen(
    toolId: String,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenSymbolCategory: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            title = toolTitle(toolId),
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
        when (toolId) {
            "symbol" -> SymbolCategoryList(onOpen = onOpenSymbolCategory)
            "pinyin" -> ConverterToolScreen(
                placeholder = "输入汉字，例如：你好世界",
                hint = "查不到的字（标点、英文、数字）会原样保留。多音字取字典里靠前的那一个读音。",
                resultLabel = "拼音结果",
                convert = { hanziToPinyin(it) },
                onCopyText = onCopyText,
            )
            "chai" -> ConverterToolScreen(
                placeholder = "输入汉字，例如：卧项功",
                hint = "只能拆左右 / 上下结构的字；表里没有的字会原样保留。",
                resultLabel = "拆分结果",
                convert = { hanziToChai(it) },
                onCopyText = onCopyText,
            )
            "daxie" -> DaxieToolScreen(onCopyText = onCopyText)
            "base64" -> Base64ToolScreen(onCopyText = onCopyText)
            "radix" -> RadixToolScreen(onCopyText = onCopyText)
            "color" -> ColorToolScreen(onCopyText = onCopyText)
            "currency" -> CurrencyToolScreen(onCopyText = onCopyText)
            "bmi" -> BmiToolScreen(onCopyText = onCopyText)
            "editor" -> CodeEditorToolScreen(onCopyText = onCopyText)
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 特殊符号：分类目录（竖向）                                                    */
/* -------------------------------------------------------------------------- */
@Composable
private fun SymbolCategoryList(onOpen: (Int) -> Unit) {
    val categories = SYMBOL_CATEGORIES
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp),
    ) {
        item(key = "symbol_hint") {
            Text(
                text = "共 ${categories.size} 个分类 · 点一个查看全部符号",
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        // 45 个分类全塞一张卡片太长：每 6 条一块、块间留空隙，
        // 按下时只有手指那一片会倾倒，不会整张巨卡一起翻。
        items(
            items = pageChunks(categories.size, per = 6),
            key = { "symbol_chunk_${it.first}" },
        ) { range ->
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                for (index in range) {
                    val category = categories[index]
                    PressableRow(
                        title = category.title,
                        summary = "${category.symbols.size} 个符号",
                        onClick = { onOpen(index) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
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

/* -------------------------------------------------------------------------- */
/* 通用「输入 → 输出」页                                                        */
/* -------------------------------------------------------------------------- */
@Composable
internal fun ConverterToolScreen(
    placeholder: String,
    hint: String,
    resultLabel: String,
    convert: (String) -> String,
    onCopyText: (String, String) -> Unit,
    extra: (@Composable () -> Unit)? = null,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val result = if (input.isBlank()) "" else convert(input)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        TiltPressTextField(
            value = input,
            onValueChange = { input = it },
            label = placeholder,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        if (extra != null) {
            Spacer(modifier = Modifier.height(8.dp))
            extra()
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = hint,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SmallTitle(text = resultLabel)
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = result.ifEmpty { "（输入后这里会实时显示结果）" },
                style = MiuixTheme.textStyles.title2,
                color = if (result.isEmpty()) {
                    MiuixTheme.colorScheme.onSurfaceContainerVariant
                } else {
                    MiuixTheme.colorScheme.onSurfaceContainer
                },
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            text = "复制结果",
            onClick = { if (result.isNotEmpty()) onCopyText(result, resultLabel) },
            enabled = result.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

/* -------------------------------------------------------------------------- */
/* 数字大写                                                                    */
/* -------------------------------------------------------------------------- */
@Composable
private fun DaxieToolScreen(onCopyText: (String, String) -> Unit) {
    var toUpperMode by rememberSaveable { mutableStateOf(true) }
    ConverterToolScreen(
        placeholder = if (toUpperMode) "输入小写金额，例如：1234.5" else "输入大写金额，例如：人民币壹仟贰佰叁拾肆元伍角",
        hint = if (toUpperMode) {
            "支持到 99999999999.99（约一千亿元），角分自动补「整」。"
        } else {
            "支持「元 / 圆」「拾佰仟万亿」，结果保留两位小数。"
        },
        resultLabel = if (toUpperMode) "大写金额" else "小写金额",
        convert = { text ->
            val out = if (toUpperMode) DaxieConverter.toUpper(text) else DaxieConverter.toLower(text)
            out ?: "格式不正确，请检查输入"
        },
        onCopyText = onCopyText,
        extra = {
            Card(modifier = Modifier.fillMaxWidth()) {
                ModeRow(
                    title = "小写转大写",
                    selected = toUpperMode,
                    onClick = { toUpperMode = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ModeRow(
                    title = "大写转小写",
                    selected = !toUpperMode,
                    onClick = { toUpperMode = false },
                )
            }
        },
    )
}

/** 二选一的选择行（竖排，选中项右侧打勾）。 */
@Composable
internal fun ModeRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    BasicComponent(
        title = title,
        onClick = onClick,
        endActions = {
            if (selected) {
                Icon(
                    imageVector = MiuixIcons.Basic.Check,
                    contentDescription = "已选择",
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
        },
    )
}
