package com.qingning.sweetlime.ui.effect

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.view.WindowInsetsController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView

/**
 * App 当前**实际生效**的深浅色，供 `enableEdgeToEdge` 的 detectDarkMode 回调读取。
 *
 * 为什么必须让 `enableEdgeToEdge` 知道：它内部注册了一个 `EdgeToEdgeCallback`，
 * 每次 **window insets 变化**（弹输入法、旋转、刘海变化、导航方式切换…）都会按
 * `detectDarkMode(resources)` 重新写一遍系统栏外观。那个默认实现读的是**系统**
 * 的 night 配置，不是 App 自己选的主题，所以我们这边 setSystemBarsAppearance
 * 设完之后，只要 insets 再变一次就被它改回去了 —— 这就是“深色模式状态栏还是黑字”。
 *
 * 把它设成 App 的深色状态之后，两边永远一致，不会被来回踩。
 */
object AppDarkMode {
    /** null = 还没算出来（此时按系统走）。 */
    @Volatile
    var isDark: Boolean? = null
}

/** 把窗口里那个 Activity 从任意一层 Context 包皮里剥出来。 */
private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return ctx as? Activity
}

/** 系统自己的深色判断（和 androidx.activity 默认实现同一套逻辑）。 */
internal fun Resources.systemIsDark(): Boolean =
    (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

@Composable
actual fun ApplySystemBarAppearance(dark: Boolean) {
    val view = LocalView.current
    // 先把状态告诉 enableEdgeToEdge 的回调，再自己立刻写一遍（不依赖它何时触发）。
    AppDarkMode.isDark = dark
    LaunchedEffect(dark, view) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        val controller = window.insetsController ?: return@LaunchedEffect
        val mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
            WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
        // 浅色底要浅色图标 → 打开 LIGHT_*；深色底要白图标 → 清掉。
        controller.setSystemBarsAppearance(if (dark) 0 else mask, mask)
    }
}
