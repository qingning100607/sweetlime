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