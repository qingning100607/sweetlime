package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.ui.effect.bounceVerticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import com.qingning.sweetlime.ui.effect.LocalTopBarInset
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
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

internal fun toolTitle(id: String): String =
    TOOL_ENTRIES.firstOrNull { it.id == id }?.title ?: tr("工具")

/** 单个工具的二级页：目录见 [TOOL_ENTRIES]，每个 id 对应一个页面。 */
@Composable
fun ToolScreen(
    toolId: String,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,
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
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        // 内容层：Haze 的采样源。顶部先空出顶栏高度，滚动时内容从顶栏下面穿过去并被实时糊。
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 采样源里先铺一层「和整页一样的底」：不然采样区是透明的，
                // 那片区域就等于没糊 —— 之前设置页顶栏看着比主页“淡”就是这个原因。
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {
            // 顶栏是浮层：把它的高度往下传，各工具页自己把那点高度写进「滚动内容」里。
            // （写在外层占位里的话，内容永远滑不到顶栏下面 → 顶栏就没有东西可糊。）
            CompositionLocalProvider(LocalTopBarInset provides topBarHeight) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (toolId) {
            "symbol" -> SymbolCategoryList(onOpen = onOpenSymbolCategory)
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
            "url" -> UrlToolScreen(onCopyText = onCopyText)
            "radix" -> RadixToolScreen(onCopyText = onCopyText)
            "color" -> ColorToolScreen(onCopyText = onCopyText)
            "currency" -> CurrencyToolScreen(onCopyText = onCopyText)
            "bmi" -> BmiToolScreen(onCopyText = onCopyText)
            "editor" -> CodeEditorToolScreen(onCopyText = onCopyText)
            // 兜底：toolId 正常只可能来自路由，走到这里说明 id 对不上
            // （路由恢复异常、版本调整过工具表等）。没有这个分支就是一整页空白，
            // 连顶栏都没有，用户只能靠系统返回键退出。
            else -> UnknownTool()
                }
            }
            }
        }

        // 顶栏浮层：实时模糊（Haze）+ 标题 + 返回。内容从它下面穿过去时会被逐帧糊掉。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
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
            SmallTopAppBar(
                // 顶栏自己不铺底：背景已经由外层铺好了。
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

/* -------------------------------------------------------------------------- */
/* 特殊符号：分类目录（竖向）                                                    */
/* -------------------------------------------------------------------------- */
@Composable
private fun SymbolCategoryList(onOpen: (Int) -> Unit) {
    val categories = SYMBOL_CATEGORIES
    val topInset = LocalTopBarInset.current
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical(),
        // 顶栏是浮层：顶栏高度写进 contentPadding，条目才能滑到顶栏下面被实时糊。
        contentPadding = PaddingValues(top = 8.dp + topInset, bottom = 48.dp),
    ) {
        item(key = "symbol_hint") {
            Text(
                text = trf("共 {} 个分类 · 点一个查看全部符号", categories.size),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item(key = "symbol_list") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                categories.forEachIndexed { index, category ->
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
                    if (index != categories.lastIndex) {
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
            .bounceVerticalScroll()
            .padding(horizontal = 12.dp),
    ) {
        // 顶栏是浮层：这点高度写在「滚动内容」里，内容才能滑到顶栏下面被实时糊。
        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = input,
            onValueChange = { input = it },
            label = placeholder,
            useLabelAsPlaceholder = true,
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
                text = result.ifEmpty { tr("（输入后这里会实时显示结果）") },
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
            text = tr("复制结果"),
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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ModeRow(
                    title = tr("大写转小写"),
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
                    contentDescription = tr("已选择"),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
        },
    )
}

/* -------------------------------------------------------------------------- */
/* 未知工具：兜底页                                                              */
/* -------------------------------------------------------------------------- */

/**
 * toolId 认不出来时的兜底。
 *
 * 正常路径不会走到这里；但路由恢复异常、或者以后工具表调整过，
 * 没有兜底就是一整页空白 —— 用户会以为应用卡死了。
 */
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
