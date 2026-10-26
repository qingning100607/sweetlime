package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable

/**
 * 让系统栏（状态栏 / 导航栏）的**图标颜色**跟 App 自己的深浅色一致。
 *
 * 为什么需要这个：`MainActivity` 里只调了无参的 `enableEdgeToEdge()`，它的默认
 * `SystemBarStyle.auto(...)` 是**跟系统深浅色**走的。可本 App 的主题是三档的
 * （跟随系统 / 浅色 / 深色，见 ThemeMode）——只要 App 选的深浅和系统不一致，
 * 系统栏图标就会反过来：深色 App 配浅色图标判断错位，状态栏就“看不见了”。
 *
 * 真机上量过：App 选深色、系统是浅色时，状态栏那一条是深色底 + 深色图标。
 */
@Composable
expect fun ApplySystemBarAppearance(dark: Boolean)
