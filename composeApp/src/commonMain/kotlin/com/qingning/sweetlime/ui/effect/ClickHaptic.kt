package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView

/**
 * 点一下的震动反馈：返回一个「响一次就完事」的 lambda。
 *
 * 和 KernelSU（Miuix 版）同一套：`performHapticFeedback(HapticFeedbackType.VirtualKey)`。
 * 只是这里直接落到 `View.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)`
 * （见 [HyperHaptics.performClick]）—— 不走 miuix 的 MIUI_HAPTIC_SLIDE 那一档，
 * 因为那档是「滑动」手感，点在按钮上太轻，KSU 的震感明显更扎实。
 * 拿不到系统反馈时再退回 LocalHapticFeedback（含 HyperOS 原生那支）。
 *
 * 用在「弹二级菜单 / 弹窗 / 下拉项」这类动作上：miuix 的 BasicComponent 与 OverlayListPopup
 * 自己都不发震动（0.9.4 的 aar 里没有 performHapticFeedback），所以要在这些地方自己响。
 */
@Composable
fun rememberClickHaptic(): () -> Unit {
    val view = LocalView.current
    val haptic = LocalHapticFeedback.current
    return remember(view, haptic) {
        {
            if (!HyperHaptics.performClick(view)) {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
            }
        }
    }
}
