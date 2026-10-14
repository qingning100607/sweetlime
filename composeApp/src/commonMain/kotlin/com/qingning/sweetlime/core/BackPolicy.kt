package com.qingning.sweetlime.core

/** 按下返回键时该做什么。 */
internal enum class BackAction {
    /** 先退二级页（交给 miuix-nav，我们不拦）。 */
    PopPage,

    /** 回到「转换」那一页。 */
    ToConvertTab,

    /** 已经在转换页了 —— 不拦，让系统退出应用。 */
    ExitApp,
}

/**
 * 返回键决策（对齐 KernelSU 的观感）：
 *
 * - 返回栈还没到底 → 先退二级页；
 * - 到底了、但不在「转换」页 → 回到「转换」页；
 * - 到底了、就在「转换」页 → 退出应用。
 *
 * 抽成纯函数是为了能单测 —— 这三条顺序反一下观感就完全不对（例如在工具页直接退出）。
 *
 * @param atRoot 返回栈是否只剩根页（没有二级页压着）
 * @param selectedTab 当前选中的底栏页（0 = 转换）
 * @param convertTab 「转换」页的索引
 */
internal fun resolveBackAction(
    atRoot: Boolean,
    selectedTab: Int,
    convertTab: Int = 0,
): BackAction = when {
    !atRoot -> BackAction.PopPage
    selectedTab != convertTab -> BackAction.ToConvertTab
    else -> BackAction.ExitApp
}
