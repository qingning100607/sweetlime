package com.qingning.sweetlime.ui.effect

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView

/** 把窗口里那个 Activity 从任意一层 Context 包皮里剥出来。 */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return ctx as? Activity
}

@Composable
actual fun ApplySystemBarAppearance(dark: Boolean) {
    val view = LocalView.current
    // 只在深浅色真的变了时才动系统栏（每次重组都设一遍会打断系统的过渡动画）。
    LaunchedEffect(dark, view) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        val controller = window.insetsController ?: return@LaunchedEffect
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // 浅色底要浅色图标 → 打开 LIGHT_*；深色底要白图标 → 清掉。
        controller.setSystemBarsAppearance(if (dark) 0 else mask, mask)
    }
}
