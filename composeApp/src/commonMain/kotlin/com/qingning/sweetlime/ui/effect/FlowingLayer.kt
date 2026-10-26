
package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 一帧「流光」的原料。
 *
 * @param base 底色。流光只画在屏幕高度的 78% 那块上，底部那一截露的是这个颜色，
 *   所以页面自己刷的时候必须先铺它，否则下半截会露出页面自己的白底。
 * @param brush 已经更新好 uniforms 的画刷（就是底层那一份）。
 */
class FlowingLayer(
    val brush: Brush,
    val base: Color,
    
    val tick: () -> Float,
)

/**
 * 当前这一帧的流光。只有「开了流光且机型支持」时才有值；
 * 由最底层的 [BgEffectBackground] 提供，页面不需要自己去拿。
 */
val LocalFlowingLayer = staticCompositionLocalOf<FlowingLayer?> { null }

/**
 * 给页面自己铺一层「实的流光」当底色 —— 也就是**二级页/设置页和主页长得一模一样**的那一步。
 *
 * 没有开流光（或者机型不支持）时退化成 `background(tint)`，页面保持自己原来的样子，
 * 观感和以前完全一样。
 *
 * 开了流光时按 [needsPageBackground] 决定画不画：静止时这一页是**透明**的，
 * 底下 BgEffectBackground 那层同一帧的同一张图直接透上来 —— 像素一致，但省掉一次
 * 全屏着色器（见文件头的说明）。
 */
@Composable
fun Modifier.flowingPageBackground(tint: Color): Modifier {
    val layer = LocalFlowingLayer.current ?: return this.background(tint)
    return drawBehind {
        // 读一下帧计时：这一笔是「每帧都要重画」的信号，删了二级页的流光就会定格。
        layer.tick()
        // 注意：这里**必须整屏不透明**地画。
        // 曾经试过「静止时不画、让底下那层透上来」（以为像素等价）—— 错：
        // 二级页是压在主页/工具页上面的，自己不画就会把**下面那一页的内容**透出来，
        // 观感就是「设置页变成悬浮窗口」。
        drawRect(tint)
        drawRect(layer.brush)
    }
}

@Composable
fun Modifier.flowingPageLayer(alpha: () -> Float = { 1f }): Modifier {
    val layer = LocalFlowingLayer.current ?: return this
    return drawBehind {
        layer.tick()
        val a = alpha().coerceIn(0f, 1f)
        if (a <= 0f) return@drawBehind
        drawRect(layer.base, alpha = a)
        drawRect(layer.brush, alpha = a)
    }
}

/**
 * 给「Haze 的采样源」铺一层和整页一模一样的底（**只铺上面那一条**）。
 *
 * 顶栏玻璃要糊的是**它下面的东西**：如果采样区里什么都没有（透明的），
 * 那片区域就是「没糊」，看起来就像顶栏没有模糊 —— 之前设置页和主页观感不一致
 * 就是这个原因。所以采样源里必须先铺底：流光开着就铺同一帧的流光，
 * 关着就铺纯主题底色（跟主页内容层用的是同一套）。
 *
 * 这一层只画 [TOP_BAND] 那一条：玻璃的采样区就在顶栏那一条，下面画了也白画，
 * 而每一次都是全屏着色器的钱。被完全盖住的页面连这一条都不用画。
 */
@Composable
fun Modifier.pageBackdropLayer(): Modifier {
    val flowing = LocalFlowingBackground.current
    if (!flowing) {
        // 关键：这里必须用 surface —— 也就是**页面根节点铺的那层底色**
        // （各页都是 `background(surface).flowingPageLayer()`）。
        // 之前写成 colorScheme.background，和 surface 不是一个色，
        // 等于在采样层里又盖了一层「比卡片还白」的底，卡片就白卡白底糊成一片、
        // 圆角看不见了。现在采样层铺的和页面底一模一样，卡片 R 角就回来了。
        return this.background(MiuixTheme.colorScheme.surface)
    }
    val layer = LocalFlowingLayer.current ?: return this.background(MiuixTheme.colorScheme.surface)
    // 和页面底一样：整屏铺（少画一点点都会在玻璃采样区里露出破绽）。
    return this.flowingPageLayer()
}

@Composable
fun Modifier.glassBarBackdrop(): Modifier {
    // 三个 Tab 的内容层同时给顶栏（Haze）和底栏（LayerBackdrop）当采样源，
    // 早先试过「只画上下两条」省开销 —— 中间那截一旦不画，页面就不透，
    // 底下的内容会透出来。整屏画，稳。
    return this.flowingPageLayer()
}
