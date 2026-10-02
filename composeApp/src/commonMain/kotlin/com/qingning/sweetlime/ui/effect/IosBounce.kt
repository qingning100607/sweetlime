package com.qingning.sweetlime.ui.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
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
 * 竖向滚动 —— 和主页转换列表**同一条链**，只在一处补缺。
 *
 * 主页那套（HomeScreen 的转换列表）：
 *   `LazyColumn(modifier = Modifier.fillMaxSize().overScrollVertical(), ...)`
 *   + 内容层 `nestedScroll(MiuixScrollBehavior)`
 *   + 全局 `LocalOverscrollFactory = MiuixOverscrollFactory`
 *
 * 关键点：`overScrollVertical()` 必须挂在滚动容器**外层**（和主页写法一致）。
 *
 * 只有一处 miuix 不管：内容**不满一屏**（没有可滚动范围）时，**底部那侧**它不接管
 * （实测：往上拖纹丝不动）。这时才用下面这份「跟手的大行程橡皮筋」补上 ——
 * 行程 420dp、前段 1:1 跟手、接近上限才变沉，松手用弹簧收回，尽量贴近原生手感。
 */
private class RubberBand {
    var offset by mutableFloatStateOf(0f)
    var limitPx = 0f

    /** 吃掉一段拖动（available.y > 0 = 手指往上拖 = 底部那侧）。 */
    fun onDrag(delta: Float): Float {
        if (delta <= 0f || limitPx <= 0f) return 0f
        val progress = (abs(offset) / limitPx).coerceIn(0f, 1f)
        // 前段 1:1 跟手，越接近上限越沉（iOS 那种"拉到头就费劲"）。
        val damped = delta * (1f - progress * 0.9f)
        offset = (offset + damped).coerceIn(0f, limitPx)
        return delta
    }

    suspend fun settle() {
        if (offset == 0f) return
        val anim = Animatable(offset)
        anim.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessMedium,
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

/** 竖向滚动 —— 与主页转换列表**完全一致**：只挂外层 overScrollVertical，其余交给 miuix。 */
@Composable
fun Modifier.bounceVerticalScroll(
    state: ScrollState = rememberScrollState(),
): Modifier = this
    .overScrollVertical()
    .verticalScroll(state)

/** LazyColumn 用：与主页转换列表完全一致，只挂外层 overScrollVertical。 */
@Composable
fun Modifier.bounceListScroll(state: LazyListState = rememberLazyListState()): Modifier =
    this.overScrollVertical()