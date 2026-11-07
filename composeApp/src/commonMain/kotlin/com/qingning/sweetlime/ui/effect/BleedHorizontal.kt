package com.qingning.sweetlime.ui.effect

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.bleedHorizontal(extra: Dp = 24.dp): Modifier = this.layout { measurable, constraints ->
    val px = extra.roundToPx()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = constraints.maxWidth + px * 2,
            maxWidth = constraints.maxWidth + px * 2,
        ),
    )
    layout(constraints.maxWidth, placeable.height) { placeable.place(-px, 0) }
}