package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.utils.overScrollVertical

/**
 * 竖向滚动 —— 与主页（RootScaffold + 转换列表）**完全同一条链**。
 *
 * 主页那套是三层：
 *   1. 全局 `LocalOverscrollFactory = MiuixOverscrollFactory`（App.kt 里全局提供）；
 *   2. 内容层 `nestedScroll(MiuixScrollBehavior().nestedScrollConnection)`（各页已挂）；
 *   3. 滚动容器外面挂 `Modifier.overScrollVertical()` —— 注意是**外层**：
 *      主页写法是 `Modifier.fillMaxSize().overScrollVertical()` 再交给列表。
 *
 * 之前二级页写成 `verticalScroll(state).overScrollVertical()`，把 overScrollVertical 挂在
 * 滚动容器**里面**，miuix 的 overscroll effect 拿不到 —— 这才是「二级页不像主页」的根因。
 *
 * 这里**不再有任何自研回弹**：一切都交给 miuix，行为和主页逐字一致。
 */
@Composable
fun Modifier.bounceVerticalScroll(
    state: ScrollState = rememberScrollState(),
): Modifier = this
    .overScrollVertical()
    .verticalScroll(state)

/** LazyColumn 用：同样只挂外层 `overScrollVertical()`，与主页的列表一致。 */
@Composable
fun Modifier.bounceListScroll(state: LazyListState): Modifier = this.overScrollVertical()