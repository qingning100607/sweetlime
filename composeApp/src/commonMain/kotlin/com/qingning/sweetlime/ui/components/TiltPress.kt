package com.qingning.sweetlime.ui.components

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.DelegatableNode
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.utils.TiltFeedback
import top.yukonga.miuix.kmp.utils.pressable

/**
 * 「圆角卡片按下去，R 角往屏幕里扎」——抄自 lyricon（Apache-2.0）首页顶部那张
 * 「激活状态卡」的手感：
 *
 * ```kotlin
 * Card(pressFeedbackType = PressFeedbackType.Tilt, onClick = {})
 * ```
 *
 * 那一步落到 miuix 里就是 [TiltFeedback]：按在哪半边，就在相反那半边打一个支点，
 * 然后整块绕支点倾斜 `tiltAmount`（默认 8°，`cameraDistance = 12 * density`，
 * 透视拉得很猛），所以手指压住哪个角，那个角就明显朝屏幕里沉下去。
 *
 * SweetLime 的列表是「一张圆角 Card 里塞好几行」的结构，按压发生在**行**上，
 * 但要让**卡片**倾斜，就必须让卡片和行共用同一个 [MutableInteractionSource]：
 * - 卡片带着 [TiltFeedback] 挂在这条源上（[TiltPressCard] 负责）；
 * - 行把自己的 clickable 也接到同一条源上（[PressableRow] 自动从
 *   [LocalCardPressSource] 里取）。
 *
 * 这样按下任意一行，PressInteraction 就会走到卡片那侧的 Tilt 节点上，
 * 整个圆角卡片按角倒下；行自己只留一层淡淡的高亮，不再各自缩一圈
 * （否则会看到「行在卡片里缩小、卡片又在倾斜」两层动画打架）。
 *
 * 用在哪些页面：**所有二级页**（分类 / 详情 / 工具 / 符号 / 搜索 / 设置 …）。
 * 主页、工具 Tab、收藏、底栏这些一级界面不要用，保持原来的 sink 手感。
 */
internal val LocalCardPressSource = staticCompositionLocalOf<MutableInteractionSource?> { null }

/**
 * 什么都不做的 [androidx.compose.foundation.Indication]。
 *
 * 卡片内部的行已经由**整张卡片**提供按压反馈了，行自己不需要再叠一层，
 * 但又不能让 [androidx.compose.foundation.LocalIndication] 落回宿主默认值
 * （万一是水波纹就会和倾斜打架），所以明确换成这个空实现。
 */
internal object NoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        object : Modifier.Node() {}

    // IndicationNodeFactory 要求实现值相等（否则每次重组都会重建节点）。
    // 这里是 object，用恒等语义即可。
    override fun equals(other: Any?): Boolean = this === other

    override fun hashCode(): Int = "SweetLime.NoIndication".hashCode()
}

/**
 * 给**任意**圆角可点元素（胶囊、色块、小方块）加「按角沉下去」。
 *
 * 注意挂载位置：必须在 `.clip(...) / .background(...)` **之前**，
 * 否则只有里面的内容会歪，圆角背景纹丝不动。
 * 点击那侧用同一条 [source] 但 `indication = null`，避免叠两层反馈。
 */
@Stable
fun Modifier.tiltOnPress(source: MutableInteractionSource): Modifier =
    this.pressable(
        interactionSource = source,
        indication = TiltFeedback(),
        delay = null,
    )

/**
 * 带「按角沉下去」的 miuix [TextField]（主页 / 详情页顶部那个输入框）。
 *
 * 输入框的按压不会产生 `PressInteraction`，所以外面套一层 [pressable] 自己派发，
 * 再把**同一条**按压源交给 [TextField]（聚焦、光标那些交互都还走它），
 * 于是按下时整个圆角输入框朝按压的角扎下去。
 */
@Composable
fun TiltPressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 3,
) {
    val source = remember { MutableInteractionSource() }
    Box(modifier = modifier.tiltOnPress(source)) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            useLabelAsPlaceholder = true,
            maxLines = maxLines,
            interactionSource = source,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/**
 * 和 miuix [Card] 长得一模一样、但按下时会「按角沉下去」的圆角卡片。
 *
 * 用法就是把原来 `Card(modifier = ...) { ... }` 换成 `TiltPressCard(modifier = ...) { ... }`；
 * 里面的 [PressableRow] 会自动接上这条按压源，不需要改它们的调用。
 *
 * 卡片里如果没有可按压的行，这个倾斜永远不会触发，等同于普通 [Card]。
 */
@Composable
fun TiltPressCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.defaultColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Card(
        // delay = null：不等「长按判定」，手指一落下就立刻开始倾斜，手感跟手。
        modifier = modifier.tiltOnPress(source),
        colors = colors,
    ) {
        CompositionLocalProvider(LocalCardPressSource provides source) {
            content()
        }
    }
}

/**
 * 把 [count] 条数据按每 [per] 条一段切开。
 *
 * 二级页的长目录（比如某个分类下几十个样式）如果全塞进一张卡片，
 * 按一下整张巨大的卡片一起倾倒，观感很突兀；切成「几行一张、中间留空隙」
 * 之后每次只有手指那一片会动，稳得多。
 */
fun pageChunks(count: Int, per: Int = 6): List<IntRange> =
    if (count <= 0 || per <= 0) {
        emptyList()
    } else {
        (0 until count step per).map { it until minOf(it + per, count) }
    }