package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * 页面底部固定多留出来的那一点「可滚动范围」。
 *
 * 为什么要留：miuix 的橡皮筋（[overScrollVertical]）不是随便拖的。反编译它的 `applyDragY`：
 *
 * ```
 * if (delta == 0 || scrollRange == 0) return      // 可滚动范围为 0 ⇒ 压根没有弹簧
 * raw   = clamp(raw + delta, -scrollRange, scrollRange)
 * ratio = min(abs(raw) / scrollRange, 1)
 * offset = sign(raw) * d(ratio) * scrollRange     // d(t) = t - t²/2 + t³/3，最大 0.833
 * ```
 *
 * 位移就是把内容整体平移，**平移出来的地方露的是页面底色**（浅色主题下就是一片白）。
 * 而位移跟「可滚动范围」正相关：内容不足一屏时范围由手指位移临时撑出来，
 * 于是往下拖多少就露多少白 —— 看起来就像「UI 被一层白盖住了」。
 *
 * 所以在底部补上这一段：内容不足一屏时正好把内容补到「一屏 + [SPRING_SLACK]」，
 * 于是范围恒为 ≈48dp，弹簧照样跟手（小距离 1:1），但最多只露 ≈40dp 的底色；
 * 内容本来就超过一屏时，这段只是页面末尾的一点留白。
 */
val SPRING_SLACK: Dp = 48.dp

/**
 * 带「有界橡皮筋」的页面列：新增工具页**一律用它**，别直接写裸的 `LazyColumn`。
 *
 * 直接用裸 `LazyColumn` + `overScrollVertical()` 的下场，就是颜文字那页曾经的样子：
 * 内容只有半屏，手指往下拖 → 整页内容跟着往下走，上面露一大片白。
 * 这里把「量内容高度 → 在末尾补留白 → 让可滚动范围恒为 [SPRING_SLACK]」固化下来，
 * 免得每个新页面都要重新踩一遍。
 *
 * - [content] 是页面主体，可以放多个组件；它会被量一次高度（不足一屏时用来补留白）。
 * - 需要「每行单独成 item」（长列表只用组合可见行）的页面别用这个，见 SymbolCategoryScreen。
 */
@Composable
fun SpringColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val viewportPx = with(density) { maxHeight.toPx() }
        val slackPx = with(density) { SPRING_SLACK.toPx() }
        // 内容实际占了多高（由下面的 Column 量出来）。
        var contentPx by remember { mutableStateOf(0f) }
        // 不足一屏时补到「一屏 + slack」，本来就超一屏时只加 slack。
        val fillerPx = (viewportPx - contentPx).coerceAtLeast(0f) + slackPx

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .overScrollVertical()
                .hyperScrollHaptic(),
            contentPadding = contentPadding,
        ) {
            item(key = "spring-column-content") {
                Column(modifier = Modifier.onSizeChanged { contentPx = it.height.toFloat() }) {
                    content()
                }
            }

            // 兜底留白：见 SPRING_SLACK 的说明。
            item(key = "spring-column-filler") {
                Spacer(modifier = Modifier.height(with(density) { fillerPx.toDp() }))
            }
        }
    }
}