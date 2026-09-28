package com.qingning.sweetlime.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardColors
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.utils.TiltFeedback
import top.yukonga.miuix.kmp.utils.pressable

/**
 * 「圆角卡片按下去，R 角往屏幕里扎」—— 抄自 lyricon（Apache-2.0）首页顶部那张
 * 「激活状态卡」的手感：
 *
 * ```kotlin
 * Card(pressFeedbackType = PressFeedbackType.Tilt, onClick = {})
 * ```
 *
 * 落到 miuix 里就是 [TiltFeedback]：按在哪半边，就在相反那半边打一个支点，
 * 整块绕支点倾斜 8°（`cameraDistance = 12 * density`，透视很猛），所以手指压住
 * 哪个角，那个 R 角就明显朝屏幕里沉下去。
 *
 * **适用范围（按用户口径收窄过两轮）**：
 * - 只保留：搜索页的**搜索框**、设置页里「外观」以外的**那几张卡片**；
 * - 其余全部回到 miuix 原生手感（`BasicComponent` 走 `LocalIndication` 提供的
 *   `MiuixIndication` 高亮：只压暗 / 提亮一层，不缩也不弹），也就是 miuix_UI 本来的样子。
 *
 * 卡片里的行**不需要**跟卡片共用按压源：miuix 的 `pressable` 用的是
 * `awaitFirstDown(requireUnconsumed = false)`，行自己消费了事件它照样收得到按下，
 * 所以卡片里放普通的 [top.yukonga.miuix.kmp.basic.BasicComponent] 就够了
 * （lyricon 上游那张卡也是这么用的）。
 */
@Stable
fun Modifier.tiltOnPress(source: MutableInteractionSource): Modifier =
    this.pressable(
        interactionSource = source,
        indication = TiltFeedback(),
        delay = null,
    )

/**
 * 带「按角沉下去」的 miuix [TextField]（**目前只用在搜索页的搜索框**）。
 *
 * 输入框本身不会产生 `PressInteraction`，所以外面套一层 [pressable] 自己派发，
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
 * 里面的行照旧用 `BasicComponent` 就行，不用改它们的调用。
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
        content()
    }
}