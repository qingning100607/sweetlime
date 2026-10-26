package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberClickHaptic(): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) {
        {
            // 走开关那一套（ToggleOn）；个别机型不支持时退回虚拟按键。
            runCatching {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
            }.onFailure {
                runCatching { haptic.performHapticFeedback(HapticFeedbackType.VirtualKey) }
            }
        }
    }
}
