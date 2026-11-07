package com.qingning.sweetlime.core

internal enum class BackAction {

    PopPage,

    ToConvertTab,

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
