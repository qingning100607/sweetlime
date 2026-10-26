package com.qingning.sweetlime.ui.effect

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

@Composable
actual fun ApplySystemBarAppearance(dark: Boolean) {
    val context = LocalContext.current
    val view = LocalView.current
    val activity = remember(context) { context.findActivity() as? ComponentActivity }

    DisposableEffect(dark, activity) {
        val act = activity
        if (act != null) {
            val detect: (Resources) -> Boolean = { dark }
            act.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detect),
                navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT, detect),
            )
            act.window.isNavigationBarContrastEnforced = false
        }
        onDispose { }
    }

    // enableEdgeToEdge 是派发到下一帧的，这里再直写一次，保证当帧就是对的颜色。
    LaunchedEffect(dark, view) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        val controller = window.insetsController ?: return@LaunchedEffect
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        controller.setSystemBarsAppearance(if (dark) 0 else mask, mask)
    }
}
