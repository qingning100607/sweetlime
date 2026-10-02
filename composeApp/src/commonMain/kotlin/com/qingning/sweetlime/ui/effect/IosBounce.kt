package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * 「内容没铺满一屏也能拉出弹性回弹」。
 *
 * miuix 自带的 [overScrollVertical] 只在**有可滚动范围**（内容比屏幕长）时才出手：
 * 内容不满一屏时滚动范围是 0，它就不接管，下拉毫无反应 —— 这就是二级页
 * （工具页 / 设置 / 详情 / 符号分类这类内容短的页）「不能弹」的原因。
 *
 * 这里补一个 [NestedScrollConnection]：只在 `state.maxValue == 0`（真的没得滚）时接管，
 * 按 iOS 手感做阻尼拖动，松手/抛掷后用弹簧动画回到 0。内容够长时**完全不介入**，
 * 交给 miuix 自己的回弹，避免双重回弹。
 */
private class RubberBand {
    var offset by mutableFloatStateOf(0f)
    var limitPx = 0f

    /** 吃掉一段拖动位移，返回实际吃掉的量。拉得越多，同样的手指位移带来的位移越小。 */
    fun onDrag(delta: Float): Float {
        if (delta == 0f || limitPx <= 0f) return 0f
        val progress = abs(offset) / limitPx
        val damped = delta * 0.55f * (1f - progress * 0.55f)
        offset = (offset + damped).coerceIn(-limitPx, limitPx)
        return delta
    }

    suspend fun settle() {
        if (offset == 0f) return
        val anim = Animatable(offset)
        anim.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMediumLow,
            ),
        ) {
            offset = value
        }
    }
}

private class RubberBandConnection(
    private val band: RubberBand,
) : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source == NestedScrollSource.Fling) return Offset.Zero
        return Offset(0f, band.onDrag(available.y))
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        band.settle()
        return Velocity.Zero
    }
}

/** 竖向滚动 + 弹性回弹（内容没铺满时也能拉）。
 * 用法直接替掉 `Modifier.verticalScroll(rememberScrollState()).overScrollVertical()` 即可。 */
@Composable
fun Modifier.bounceVerticalScroll(
    state: ScrollState = rememberScrollState(),
): Modifier {
    val band = remember { RubberBand() }
    val connection = remember(band) { RubberBandConnection(band) }
    val density = LocalDensity.current
    band.limitPx = with(density) { 200.dp.toPx() }

    // 只有「真的没得滚」时才自己接管；内容够长就交给 miuix，避免双重回弹。
    val takeOver = state.maxValue == 0
    return if (takeOver) {
        this
            .nestedScroll(connection)
            .graphicsLayer { translationY = band.offset }
            .verticalScroll(state)
            .overScrollVertical()
    } else {
        this
            .verticalScroll(state)
            .overScrollVertical()
    }
}

/**
 * 给 **LazyColumn** 这类「没有 ScrollState」的列表用的修饰符：
 * 内容不够长（上下都滚不动）时也能拉出弹性回弹。
 *
 * `nestedScroll` 写在链上就等于挂在列表外面，滚得动时它什么也不做（交给列表 + miuix 回弹），
 * 滚不动时才接管，做阻尼拖动 + 弹簧回弹。
 */
@Composable
fun Modifier.bounceListScroll(state: LazyListState): Modifier {
    val canScroll = state.canScrollForward || state.canScrollBackward
    if (canScroll) return this
    val band = remember { RubberBand() }
    val connection = remember(band) { RubberBandConnection(band) }
    val density = LocalDensity.current
    band.limitPx = with(density) { 200.dp.toPx() }
    return this
        .nestedScroll(connection)
        .graphicsLayer { translationY = band.offset }
}

/**
 * 给 **LazyColumn** 这类「没有 ScrollState」的列表用的外壳：
 * 内容不够长（上下都滚不动）时也能拉出弹性回弹。
 *
 * 用 `listState.canScrollForward / canScrollBackward` 判断有没有得滚：
 * 有得滚 → 原样交给列表自己 + miuix 的回弹；没得滚 → 接管，做阻尼拖动 + 弹簧回弹。
 */
@Composable
fun BounceListContainer(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val canScroll = listState.canScrollForward || listState.canScrollBackward
    if (canScroll) {
        Box(modifier) { content() }
        return
    }
    val band = remember { RubberBand() }
    val connection = remember(band) { RubberBandConnection(band) }
    val density = LocalDensity.current
    band.limitPx = with(density) { 200.dp.toPx() }
    Box(
        modifier = modifier
            .nestedScroll(connection)
            .graphicsLayer { translationY = band.offset },
    ) {
        content()
    }
}