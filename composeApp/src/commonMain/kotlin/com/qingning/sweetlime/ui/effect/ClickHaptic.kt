package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * 点击的震动反馈：返回一个「响一次就完事」的 lambda。
 *
 * **跟 miuix 的开关（Switch）走同一条路**：`LocalHapticFeedback.performHapticFeedback(ToggleOn)`。
 * 反编译 miuix 0.9.4 的 `SwitchKt` 可以看到，它切换时做的就是
 * `LocalHapticFeedback.current.performHapticFeedback(HapticFeedbackType.ToggleOn / ToggleOff)`，
 * 所以弹二级菜单 / 选中下拉项时的震感与拨开关完全一致。
 *
 * 以前这里用 `View.performHapticFeedback(VIRTUAL_KEY)`（照 KernelSU 抄的那版），
 * 那条绕过了 `LocalHapticFeedback`，也就绕过了本项目在 App 根上提供的
 * [HyperHapticFeedback]（HyperOS 原生 miuix 那一支），震感自然跟开关对不上。
 *
 * 用在「弹二级菜单 / 弹窗 / 下拉项」这类动作上：miuix 的 BasicComponent 与 OverlayListPopup
 * 自己都不发震动（0.9.4 的 aar 里没有 performHapticFeedback），所以要在这些地方自己响。
 */
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
