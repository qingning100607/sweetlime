/*
 * SweetLime 原创文件（不是从上游拷贝的文件）。
 *
 * 背景：上游 lyricon 是给需要流光的页面**自己**套一个 BgEffectBackground 当底色的 ——
 * 见 app/src/main/kotlin/io/github/proify/lyricon/app/ui/about/AboutScreen.kt:178，
 * 「关于页」就是这么做的。这样那一页的背景是一层**实的**流光，不会把被它盖住的页面
 * 透出来（而页面只在卡片上取 surfaceContainer 的半透明色，卡片之外就是纯流光）。
 *
 * SweetLime 照这个思路做，但只用一个 shader 实例：最底层的 BgEffectBackground 每帧
 * 更新完着色器的 uniforms（resolution / colors / 时间）之后，把**那一帧的画刷**和
 * 底色通过 LocalFlowingLayer 交给下面的内容；页面拿同一支画刷把自己再刷一遍，
 * 于是：
 *   1. 页面是「实的」（完全不透明），不会再看到被盖住的上一页；
 *   2. 和主页/底层是同一帧、同一组颜色、同一个 resolution —— 像素级一致，
 *      不会出现「两套流光颜色对不上」的观感问题（也不用起第二个 shader）。
 */
package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

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
)

/**
 * 当前这一帧的流光。只有「开了流光且机型支持」时才有值；
 * 由最底层的 [BgEffectBackground] 提供，页面不需要自己去拿。
 */
val LocalFlowingLayer = staticCompositionLocalOf<FlowingLayer?> { null }

/**
 * 给页面自己铺一层「实的流光」当底色 —— 也就是**二级页/设置页和主页长得一模一样**的那一步。
 *
 * 没有开流光（或者机型不支持）时原样返回，页面保持自己原来的 `background(surface)`，
 * 观感和以前完全一样。
 *
 * 画在 `background(...)` 之后、内容之前：先把主题底色压上（保证不透），
 * 再叠同一帧的流光。
 */
@Composable
fun Modifier.flowingPageLayer(): Modifier {
    val layer = LocalFlowingLayer.current ?: return this
    return drawBehind {
        drawRect(layer.base)
        drawRect(layer.brush)
    }
}
