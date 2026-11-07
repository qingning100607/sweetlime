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

@Composable
fun TiltPressCard(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.defaultColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    val source = remember { MutableInteractionSource() }
    Card(

        modifier = modifier.tiltOnPress(source),
        colors = colors,
    ) {
        content()
    }
}