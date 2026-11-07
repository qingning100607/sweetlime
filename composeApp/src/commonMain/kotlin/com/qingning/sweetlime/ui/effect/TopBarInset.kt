package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val LocalTopBarInset = compositionLocalOf { 0.dp }

@Composable
fun TopBarInsetSpacer() {
    val inset = LocalTopBarInset.current
    if (inset > 0.dp) {
        Spacer(modifier = Modifier.height(inset))
    }
}
