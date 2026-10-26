package com.qingning.sweetlime.ui.effect

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
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

/**
 * 系统栏（状态栏 / 导航栏）外观 —— **完全照 KernelSU 的做法**：
 *
 * KernelSU 不是在自己 Activity 的 `onCreate` 里调 `enableEdgeToEdge()`，而是在
 * `setContent {}` **里面**用 `DisposableEffect(darkMode)` 调，并且把
 * `detectDarkMode = { darkMode }` 直接绑死成「App 自己的深浅色」。
 *
 * 这么写有两层作用：
 * 1. `enableEdgeToEdge` 会重新注册/刷新内部那个 EdgeToEdgeCallback，让它在之后的
 *    insets 变化时也按 App 的深浅色算，而不是按系统的 night 配置；
 * 2. 深浅色一改（设置里切主题）就重新跑一遍，立刻生效，不用重启 Activity。
 *
 * 之前两版失败的原因就在这里：只调一次、且回调读的是系统 night 配置。
 */
@Composable
actual fun ApplySystemBarAppearance(dark: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current

    DisposableEffect(dark) {
        val activity = context.findActivity() as? ComponentActivity
        if (activity != null) {
            val detect: (android.content.res.Resources) -> Boolean = { dark }
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detect),
                navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detect),
            )
            // 同 KernelSU：别让系统给导航栏加那层半透明底。
            activity.window.isNavigationBarContrastEnforced = false
        }
        onDispose { }
    }

    // 再直接写一次（enableEdgeToEdge 是异步派发的，切主题那一帧可能还没生效）。
    LaunchedEffect(dark, view) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        val controller = window.insetsController ?: return@LaunchedEffect
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        controller.setSystemBarsAppearance(if (dark) 0 else mask, mask)
    }
}
