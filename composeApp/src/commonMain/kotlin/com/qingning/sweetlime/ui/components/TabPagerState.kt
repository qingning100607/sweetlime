package com.qingning.sweetlime.ui.components

import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.utils.springAnimateToPage

/**
 * 底栏选中的「逻辑页」与 Pager 的真实页分开管理。
 *
 * 移植自 KernelSU Manager（`manager/app/src/main/java/me/weishu/kernelsu/ui/component/bottombar/BottomBar.kt`
 * 里的 `MainPagerState`，Apache-2.0，按其第 4(b) 条声明做了修改：仅换包名、去掉对 KernelSU 其它工具的依赖）。
 *
 * 为什么不直接用 `pagerState.currentPage` 当选中态：
 *  - 点一下底栏，图标要**立刻**跟着手指走，而页面还要滑一小段 —— 两者不是同一时刻；
 *  - 连点（工具→收藏→转换）时，若让 pager 在动画途中回写选中态，
 *    旧值会把图标拽回去，表现就是「画面和图标不跟手」。
 *
 * 所以：点一下先改 [selectedPage]（图标立刻过去），动画在另一条协程里跑（[navJob]，连点先取消上一段），
 * 只有**不在导航中**（[isNavigating] = false）时，才允许 pager 把真实页回写进 [selectedPage]。
 */
class TabPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
    private val animatePageChanges: Boolean = true,
) {
    /** 底栏要读的选中页：点一下立刻变，不等动画。 */
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    /** 是否正在执行「点底栏 -> 带动画滚过去」这段导航。 */
    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    /** 点底栏：图标立刻过去，页面走 KernelSU 同款弹簧动画（连点会取消上一段）。 */
    fun animateToPage(targetIndex: Int) {
        if (targetIndex == selectedPage) return

        navJob?.cancel()

        selectedPage = targetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext[Job]
            try {
                if (animatePageChanges) {
                    pagerState.springAnimateToPage(targetIndex)
                } else {
                    pagerState.scrollToPage(targetIndex)
                }
            } finally {
                // 只有「还是这一段导航」时才收尾，避免被后来的点击覆盖。
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != targetIndex) {
                        selectedPage = pagerState.currentPage
                    }
                }
            }
        }
    }

    /** 手滑翻页后回写选中态；导航进行中不回写（否则会把图标拽回去）。 */
    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }
}

@Composable
fun rememberTabPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    animatePageChanges: Boolean = true,
): TabPagerState = remember(pagerState, coroutineScope, animatePageChanges) {
    TabPagerState(pagerState, coroutineScope, animatePageChanges)
}