package com.qingning.sweetlime.core

import androidx.compose.runtime.Composable

/**
 * 系统返回键 / 返回手势（跨平台入口）。
 *
 * 用 expect/actual 而不是直接引 Compose Multiplatform 那个还在实验期的 common BackHandler：
 * Android 侧就用 `androidx.activity.compose.BackHandler`（已经依赖了 activity-compose），
 * 稳、也不用给整个模块开实验开关。
 *
 * [enabled] 为 false 时**完全不拦**，事件继续往下走（最终由系统处理 = 退出应用）。
 */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
