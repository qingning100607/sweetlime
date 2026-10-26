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

class TabPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope,
    private val animatePageChanges: Boolean = true,
) {
    /** 底栏要读的选中页：点击立刻变，不等动画。 */
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    /** 是否正在执行「点底栏 -> 带动画滚过去」这段导航。 */
    var isNavigating by mutableStateOf(false)
        private set

    private var navJob: Job? = null

    
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