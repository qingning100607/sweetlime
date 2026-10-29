package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import top.yukonga.miuix.kmp.utils.overScrollVertical
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.readClipboard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.core.codePointLength

/**
 * 详情页：一个样式 + 一段原文 = 一个结果。
 *
 * **原文是能改的。** 从主页、分类、搜索、收藏、最近使用 —— 不管从哪进来，
 * 「原文」那一栏都是一个可编辑的输入框，边改边出效果：
 * 点「最近使用」进去就是想图个方便，进来还能顺手换个字看效果，不然点进去干嘛。
 *
 * 初始原文由调用方给（主页输入框 / 搜索框里那行字），改动时通过 [onInputChange]
 * 回写给上层，所以返回后主页输入框里也是你刚改的那段字，不会前后不一致。
 *
 * @param transform 当前样式。
 * @param initialInput 初始原文。
 * @param isFavorite 判断「当前这段原文 + 这个样式」有没有被收藏。
 * @param onToggleFavorite 收藏 / 取消收藏当前这条。
 * @param onInputChange 原文改动时回写（同步到主页输入框）。
 * @param onCopy 复制当前效果。
 * @param onShare 分享当前效果。
 */
@Composable
fun DetailScreen(
    transform: TextTransform,
    initialInput: String,
    isFavorite: (String) -> Boolean,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 本地可编辑原文：用样式 id 作 key，换样式时重置为该样式带进来的原文。
    var text by rememberSaveable(transform.id) { mutableStateOf(initialInput) }
    // 转换放到后台线程算：算完再画内容。加载动画只盖这段**真实**耗时，不掺固定延时。
    var output by remember(transform.id) { mutableStateOf<String?>(null) }
    // 加载动画的最短时长：真算得快也要走完这一段；真算得慢就等它算完，不会被截断。
    var minElapsed by remember(transform.id) { mutableStateOf(false) }
    LaunchedEffect(transform.id) {
        delay(LOADING_MIN_MS)
        minElapsed = true
    }
    LaunchedEffect(transform.id, text) {
        // 先让出一帧：让加载态真的画到屏幕上，再去算。
        withFrameNanos { }
        val computed = withContext(Dispatchers.Default) { transform.transform(text) }
        // 先让出一帧，让加载态真正画到屏幕上，再切到内容。
        withFrameNanos { }
        output = computed
    }
    val contentReady = output != null
    val resolved = output.orEmpty()
    val hasEffect = contentReady && resolved != text && text.isNotEmpty()
    // 效果正文：分段懒加载的准备（屏幕外的段不排版、不绘制）。
    val effectText = when {
        text.isEmpty() -> tr("上面输入文字，这里就是转换后的效果。")
        hasEffect -> resolved
        else -> tr("这段文字在这个样式里没有变化。")
    }
    val effectChunks = remember(effectText) { splitForDisplay(effectText) }
    // isFavorite 内部会跑一次 transform 来拼 key：放进 remember，
    // 免得每次重组都在主线程重算一遍（复杂样式会直接卡住圆环）。
    val favorite = remember(text, transform.id) { isFavorite(text) }

    // 加载动画：真实计算 + 最短 400ms，两个都满足才显示内容。
    val showSpinner = !contentReady || !minElapsed
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {
        // 内容层 = Haze 的采样源。整页铺满，滚动时卡片从顶栏**下面穿过去**并被实时糊掉
        // —— 这就是主页顶栏那种模糊。采样层单独一层、不参与滚动。
        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {
        if (showSpinner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = topBarHeight),
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
        } else {
        // 出场动画：和分类页一样的 300ms 淡入 + 轻微上移，不要直接跳出一整屏。
        var contentVisible by remember(transform.id) { mutableStateOf(false) }
        LaunchedEffect(transform.id, contentReady, minElapsed) {
            if (contentReady && minElapsed) {
                withFrameNanos { }
                contentVisible = true
            }
        }
        val contentAlpha by animateFloatAsState(
            targetValue = if (contentVisible) 1f else 0f,
            animationSpec = tween(CONTENT_FADE_MS, easing = FastOutSlowInEasing),
            label = "detailContentAlpha",
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = CONTENT_SLIDE_PX * (1f - contentAlpha)
                }
                .overScrollVertical()
                .hyperScrollHaptic(),
            contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
        ) {
            item {
                Column {

            // 顶栏高度写在「滚动内容」里（不是外层占位），内容才能滚到顶栏下面去。
            Spacer(modifier = Modifier.height(topBarHeight))

SmallTitle(text = tr("原文"))
            Card(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onInputChange(it)
                    },
                    label = tr("输入要转换的文字"),
                    useLabelAsPlaceholder = true,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                // 跟主页一样的一排小工具：字数 + 粘贴 + 清空。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = trf("{} 字", text.codePointLength()),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            readClipboard()?.let {
                                text = it
                                onInputChange(it)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Paste,
                            contentDescription = tr("粘贴"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                text = ""
                                onInputChange("")
                            },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Clear,
                                contentDescription = tr("清空"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                }
            }

        }
        }
        // 效果标题单独一个 item。
        item {
            SmallTitle(text = tr("效果"))
        }
        // 效果正文分段懒加载：一段一个 item，屏幕外的段不排版、不绘制。
        // 段与段之间无缝拼接（首段圆上角、末段圆下角、中间平角），和一张整卡观感一致。
        if (hasEffect && effectChunks.size > 1) {
            itemsIndexed(effectChunks) { index, chunk ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(effectChunkShape(index, effectChunks.size)),
                    cornerRadius = 0.dp,
                ) {
                    Text(
                        text = chunk,
                        style = MiuixTheme.textStyles.title2,
                        color = MiuixTheme.colorScheme.onSurfaceContainer,
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = if (index == 0) 16.dp else 0.dp,
                            bottom = if (index == effectChunks.size - 1) 16.dp else 0.dp,
                        ),
                    )
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = effectText,
                        style = MiuixTheme.textStyles.title2,
                        color = if (hasEffect) {
                            MiuixTheme.colorScheme.onSurfaceContainer
                        } else {
                            MiuixTheme.colorScheme.onSurfaceContainerVariant
                        },
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }
        // 字数额 + 底部按钮
        item {
            Column {
            // 输出也标一个字数额（按码点算，一个 emoji 算一个字）。
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = trf("{} 字", resolved.codePointLength()),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = tr("复制"),
                    onClick = { onCopy(resolved) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
                TextButton(
                    text = tr("分享"),
                    onClick = { onShare(resolved) },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = if (favorite) tr("取消收藏") else tr("收藏"),
                    onClick = { onToggleFavorite(text) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        
                }
            }
        }
        }
        }

        // 顶栏浮层：实时模糊（Haze）+标题 +返回。卡片从它下面穿过去时逐帧被糊掉。
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
            // 兜底：过渡动画（预测性返回预览）时页面是被平移的，Haze 采到的是平移**之前**的画面，
            // 顶栏最上面那一条（状态栏那条）于是会把旧位置的文字透出来。
            // 这里在玻璃之上再蒙一层自上而下由浓变无的主题底色，把那一条压实；
            // 静止时下面本来就有模糊 + 主题色，观感几乎不变。
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
                // 顶栏自己不铺底：二级页的背景已经是「实流光」了，铺底会把顶部那块盖成纯白。
                color = Color.Transparent,
                title = transform.title,
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

/** 详情页加载动画的直径。 */
private val DETAIL_LOADING_SIZE = 44.dp

/** 加载动画的最短时长（毫秒）：真算得快也走完；真算得慢就以实际算完为准。 */
private const val LOADING_MIN_MS = 400L

/** 内容淡入时长（毫秒）。 */
private const val CONTENT_FADE_MS = 300

/** 出场时内容从下方约 12dp 滑到位的像素偏移。 */
private const val CONTENT_SLIDE_PX = 36f

/** 效果正文按大约这么多字符切一段（只为少排版，不改内容）。 */
private const val EFFECT_CHUNK_SIZE = 800

/** 按段切开，尽量切在换行处，绝不切在代理对中间（emoji 会被切坏）。 */
private fun splitForDisplay(source: String): List<String> {
    if (source.length <= EFFECT_CHUNK_SIZE) return listOf(source)
    val out = ArrayList<String>(source.length / EFFECT_CHUNK_SIZE + 1)
    var start = 0
    while (start < source.length) {
        var end = minOf(start + EFFECT_CHUNK_SIZE, source.length)
        if (end < source.length && source[end].isLowSurrogate()) end += 1
        val newline = source.lastIndexOf('\n', end - 1)
        if (newline > start + EFFECT_CHUNK_SIZE / 3) end = newline + 1
        out.add(source.substring(start, end))
        start = end
    }
    return out
}

/** 分段卡片的圆角：首段只圆上角、末段只圆下角、中间平角，拼起来就是一张卡。 */
private fun effectChunkShape(index: Int, count: Int): Shape {
    val r = 16.dp
    return when {
        count <= 1 -> RoundedCornerShape(r)
        index == 0 -> RoundedCornerShape(topStart = r, topEnd = r)
        index == count - 1 -> RoundedCornerShape(bottomStart = r, bottomEnd = r)
        else -> RectangleShape
    }
}
