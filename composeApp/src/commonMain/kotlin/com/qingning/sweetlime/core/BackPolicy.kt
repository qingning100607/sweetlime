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

internal fun resolveBackAction(
    atRoot: Boolean,
    selectedTab: Int,
    convertTab: Int = 0,
): BackAction = when {
    !atRoot -> BackAction.PopPage
    selectedTab != convertTab -> BackAction.ToConvertTab
    else -> BackAction.ExitApp
}
