package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.lazy.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.CompositionLocalProvider
import com.qingning.sweetlime.ui.effect.LocalTopBarInset
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.ui.graphics.Brush
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TOOL_ENTRIES
import com.qingning.sweetlime.core.mapping.KAOMOJI_RANGE
import com.qingning.sweetlime.core.mapping.KAOMOJI_START
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES
import com.qingning.sweetlime.core.mapping.SYMBOL_MORE_RANGE
import com.qingning.sweetlime.core.tools.DaxieConverter
import com.qingning.sweetlime.core.tools.hanziToChai
import com.qingning.sweetlime.core.tools.hanziToPinyin
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
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
import com.qingning.sweetlime.core.codePointLength

internal fun toolTitle(id: String): String =
    TOOL_ENTRIES.firstOrNull { it.id == id }?.title ?: tr("工具")

@Composable
fun ToolScreen(
    toolId: String,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,

    onMessage: (String) -> Unit = {},
    onOpenSymbolCategory: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()

                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {

            CompositionLocalProvider(LocalTopBarInset provides topBarHeight) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (toolId) {
            "symbol" -> SymbolCategoryList(
                sections = SYMBOL_SECTIONS + SYMBOL_MORE_SECTIONS,
                onOpen = onOpenSymbolCategory,
            )
            "kaomoji" -> SymbolCategoryList(
                sections = KAOMOJI_SECTIONS,
                onOpen = onOpenSymbolCategory,
            )
            "symbol_more" -> SymbolCategoryList(
                sections = SYMBOL_MORE_SECTIONS,
                onOpen = onOpenSymbolCategory,
            )
            "pinyin" -> ConverterToolScreen(
                placeholder = tr("输入汉字，例如：你好世界"),
                hint = tr("查不到的字（标点、英文、数字）会原样保留。多音字取字典里靠前的那一个读音。"),
                resultLabel = tr("拼音结果"),
                convert = { hanziToPinyin(it) },
                onCopyText = onCopyText,
            )
            "chai" -> ConverterToolScreen(
                placeholder = tr("输入汉字，例如：卧项功"),
                hint = tr("只能拆左右 / 上下结构的字；表里没有的字会原样保留。"),
                resultLabel = tr("拆分结果"),
                convert = { hanziToChai(it) },
                onCopyText = onCopyText,
            )
            "daxie" -> DaxieToolScreen(onCopyText = onCopyText)
            "base64" -> Base64ToolScreen(onCopyText = onCopyText)
            "md5" -> Md5ToolScreen(onCopyText = onCopyText)
            "qqname" -> QqNameToolScreen(onCopyText = onCopyText)
            "level" -> LevelToolScreen()
            "url" -> UrlToolScreen(onCopyText = onCopyText)
            "radix" -> RadixToolScreen(onCopyText = onCopyText)
            "color" -> ColorToolScreen(onCopyText = onCopyText)
            "dup" -> DupCheckToolScreen(onCopyText = onCopyText)
            "currency" -> CurrencyToolScreen(onCopyText = onCopyText)
            "bmi" -> BmiToolScreen(onCopyText = onCopyText)

            else -> UnknownTool()
                }
            }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bleedHorizontal()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 20.dp,
                            noiseFactor = 0.15f,
                            tint = HazeTint(hazeTint.copy(alpha = 0.30f)),
                        ),
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to MiuixTheme.colorScheme.surface.copy(alpha = 0.55f),
                            0.6f to Color.Transparent,
                        ),
                    ),
            )
            SmallTopAppBar(

                color = Color.Transparent,
                title = toolTitle(toolId),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = tr("返回"),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun SymbolCategoryList(sections: List<Pair<String, List<Int>>>, onOpen: (Int) -> Unit) {
    val categories = SYMBOL_CATEGORIES
    val topInset = LocalTopBarInset.current
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),

        contentPadding = PaddingValues(top = 8.dp + topInset, bottom = 48.dp),
    ) {
        item(key = "symbol_hint") {
            Text(
                text = trf("共 {} 个分类 · 点一个查看全部符号", sections.sumOf { it.second.size }),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        sections.forEachIndexed { sectionIndex, (title, indices) ->
            item(key = "symbol_section_$sectionIndex") {
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
                        indices.forEach { index ->
                            val category = categories.getOrNull(index) ?: return@forEach
                            BasicComponent(
                                title = category.title,
                                summary = trf("{} 个符号", category.symbols.size),
                                onClick = { onOpen(index) },
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

@Composable
internal fun ConverterToolScreen(
    placeholder: String,
    hint: String,
    resultLabel: String,
    convert: (String) -> String,
    onCopyText: (String, String) -> Unit,
    extra: (@Composable () -> Unit)? = null,

    extraForInput: (@Composable (String) -> Unit)? = null,
) {
    var input by rememberSaveable { mutableStateOf("") }
    val result = if (input.isBlank()) "" else convert(input)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column {

        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = input,
            onValueChange = { input = it },
            label = placeholder,
            useLabelAsPlaceholder = true,
            maxLines = 4,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        )
        if (extra != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                extra()
            }
        }
        if (extraForInput != null && input.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            extraForInput(input)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = hint,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
        SmallTitle(text = resultLabel)
        if (result.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text(
                    text = result,
                    style = MiuixTheme.textStyles.title2,
                    color = MiuixTheme.colorScheme.onSurfaceContainer,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        if (result.isNotEmpty()) {
            Text(
                text = trf("{} 字", result.codePointLength()),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            text = tr("复制结果"),
            onClick = { if (result.isNotEmpty()) onCopyText(result, resultLabel) },
            enabled = result.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        Spacer(modifier = Modifier.height(48.dp))

            }
        }
    }
}

@Composable
private fun DaxieToolScreen(onCopyText: (String, String) -> Unit) {
    var toUpperMode by rememberSaveable { mutableStateOf(true) }
    ConverterToolScreen(
        placeholder = if (toUpperMode) tr("输入小写金额，例如：1234.5") else tr("输入大写金额，例如：人民币壹仟贰佰叁拾肆元伍角"),
        hint = if (toUpperMode) {
            tr("支持到 99999999999.99（约一千亿元），角分自动补「整」。")
        } else {
            tr("支持「元 / 圆」「拾佰仟万亿」，结果保留两位小数。")
        },
        resultLabel = if (toUpperMode) tr("大写金额") else tr("小写金额"),
        convert = { text ->
            val out = if (toUpperMode) DaxieConverter.toUpper(text) else DaxieConverter.toLower(text)
            out ?: tr("格式不正确，请检查输入")
        },
        onCopyText = onCopyText,
        extra = {
            Card(modifier = Modifier.fillMaxWidth()) {
                ModeRow(
                    title = tr("小写转大写"),
                    selected = toUpperMode,
                    onClick = { toUpperMode = true },
                )
                ModeRow(
                    title = tr("大写转小写"),
                    selected = !toUpperMode,
                    onClick = { toUpperMode = false },
                )
            }
        },
    )
}

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
                    contentDescription = tr("已选择"),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
        },
    )
}

@Composable
private fun UnknownTool() {
    Column(modifier = Modifier.fillMaxSize()) {
        SmallTitle(text = tr("出错了"))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
        ) {
            BasicComponent(
                title = tr("这个工具不存在"),
                summary = tr("可能是版本更新后工具列表调整过。返回上一页重新进一次就好。"),
            )
        }
    }
}

private val SYMBOL_SECTIONS: List<Pair<String, List<Int>>> = listOf(
    "常用与热门" to (0..1).toList(),
    "图案与表情" to (2..8).toList(),
    "生活与娱乐" to (9..17).toList(),
    "数学与单位" to (18..23).toList(),
    "星座与形状" to (24..30).toList(),
    "文字与符号" to (31..43).toList(),
)

private val SYMBOL_MORE_SECTIONS: List<Pair<String, List<Int>>> = listOf(
    "新增分类" to SYMBOL_MORE_RANGE.toList(),
)

private val KAOMOJI_SECTIONS: List<Pair<String, List<Int>>> = listOf(
    "情绪" to (KAOMOJI_START..KAOMOJI_START + 4).toList(),
    "动作与动物" to (KAOMOJI_START + 5..KAOMOJI_START + 10).toList(),
    "其他" to (KAOMOJI_START + 11..KAOMOJI_RANGE.last).toList(),
)
