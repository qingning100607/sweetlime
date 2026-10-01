// 移植自 KernelSU Manager（https://github.com/tiann/KernelSU，Apache-2.0），
// 上游又改编自 compose-miuix-ui 的 IosLiquidGlassNavigationBar 示例（Apache-2.0）。
// 按 Apache-2.0 第 4(b) 条声明修改：仅把包名换成 SweetLime 的、去掉对 KernelSU 主题工具的依赖。
// Adapted from Kyant0/AndroidLiquidGlass — https://github.com/Kyant0/AndroidLiquidGlass (Apache 2.0).
// Mirrored from compose-miuix-ui example.

package com.qingning.sweetlime.ui.components.liquid

import top.yukonga.miuix.kmp.blur.BackdropEffectScope
import top.yukonga.miuix.kmp.blur.colorControls

fun BackdropEffectScope.vibrancy() {
    colorControls(
        brightness = 0f,
        contrast = 1f,
        saturation = 1.5f,
    )
}
