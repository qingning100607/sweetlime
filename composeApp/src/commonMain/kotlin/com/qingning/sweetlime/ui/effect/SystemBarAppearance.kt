package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable

/**
 * 让系统栏（状态栏 / 导航栏）的**图标颜色**跟 App 自己的深浅色一致。
 *
 * 为什么需要这个：`enableEdgeToEdge()` 默认的 `SystemBarStyle.auto(...)` 是**跟系统深浅色**
 * 走的，而本 App 的主题是三档（跟随系统 / 浅色 / 深色，见 ThemeMode）。只要 App 选的
 * 深浅和系统不一致，系统栏图标就会反向：深色界面配黑色图标 → 那一条“看不见”。
 *
 * 所以调用方传进来的 [dark] 必须是「App 实际显示的深浅色」，由 Theme.kt 按 themeMode +
 * 系统夜间模式算出（不要用 miuix 的 ThemeController.isDark，它不跟随 App 的设置）。
 */
@Composable
expect fun ApplySystemBarAppearance(dark: Boolean)
