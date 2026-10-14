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
 *
 * ---------------------------------------------------------------------------
 * 2026-10 追加：**这套「每页自己刷一遍」的代价太高了**（实测每帧好几次全屏着色器），
 * 于是试过「只在看得见的地方画」。结论：**这条路走不通，已全部回退** ——
 *
 * 全屏着色器在本机是 1440×3200 ≈ 460 万像素/次，而一次「进二级页」的画面上同时有：
 * 底层 1 次 + 二级页自己 1 次 + 这一页的顶栏采样底又 1 次 = 3 次/帧；过渡中两个页面
 * 同时在场就是 5～6 次/帧。快速连点「进页 → 返回」时 GPU 直接拉满（实测平均忙碌 45%），
 * 掉帧就是从这里来的 —— 和顶栏那层高斯模糊关系不大（它只糊顶栏那一块）。
 *
 * 参考 KernelSU：它的页面就是普通 Scaffold + 纯色底，过渡里没有任何全屏特效，
 * 所以怎么快点都不心疼。我们保留流光观感，但做到**只在真正看得见的地方画**：
 *
 * 页面必须**整屏不透明**地画自己的底色。曾经按页面角色（静止 / 被推走 / 被盖住）
 * 少画一部分，理由是「底下那层铺的是同一帧同一张图，像素等价」——
 * 这只对**没有下层页面**的情况成立；二级页压在主页/工具页上时，
 * 少画的地方会把**下面那一页的内容**透出来（「设置页变成悬浮窗口」就是这么来的）。
 *
 * 另外「只给玻璃画采样那一条」也行不通：那一层本身就是可见的页面底，
 * 少画一条就少一块，露出来的是底下那页的内容。
 *
 * 所以：**宁可多花这笔开销，也不要在这里省**。真要降开销，只动时间维度
 * （见 FrameTimeSeconds 里的 30fps 上限），不要动「画哪一块」。
 *
 * 本机实测（1440×3200，快速「进二级页 → 返回」×15，读 /sys/class/kgsl/kgsl-3d0）：
 * - 静止不动：GPU 平均忙碌 ≈ 0.9%；
 * - 快速进/退：底栏玻璃 +9 个百分点，流光 +8～17 个百分点；
 * - 把顶栏模糊半径临时改成 0：55% → 37%（**顶栏模糊是过渡里最大的一块**）。
 *   这一项**按需求保持原样、不做优化** —— 只在这里留个记录，以后要动的时候有据可查。
 */
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
    /**
     * 帧计时（秒）的读取器。**必须在 draw 里读一次**：
     *
     * 着色器的 uniforms 是底层那层每帧更新的，但「页面自己刷的那一遍」如果什么 state
     * 都不读，Compose 会认为这个 draw 不需要重画（NavDisplay 还给每个页面套了离屏图层），
     * 于是那一遍只在第一帧执行过 —— 观感就是「主页在流动，二级页/设置页的流光却是定格的」。
     *
     * 读一下它，页面这一层就每帧跟着重画，和主页完全同步（上游给关于页套的是第二个
     * BgEffectBackground 实例，它内部同样每帧读帧计时，所以本来就没有这个问题）。
     */
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
