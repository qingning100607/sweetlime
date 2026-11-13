package com.qingning.sweetlime.ui.effect

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.utils.overScrollVertical

val SPRING_SLACK: Dp = 48.dp

@Composable
fun SpringColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val viewportPx = with(density) { maxHeight.toPx() }
        val slackPx = with(density) { SPRING_SLACK.toPx() }

        var contentPx by remember { mutableStateOf(0f) }

        val fillerPx = (viewportPx - contentPx).coerceAtLeast(0f) + slackPx

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .overScrollVertical()
,
            contentPadding = contentPadding,
        ) {
            item(key = "spring-column-content") {
                Column(modifier = Modifier.onSizeChanged { contentPx = it.height.toFloat() }) {
                    content()
                }
            }

            item(key = "spring-column-filler") {
                Spacer(modifier = Modifier.height(with(density) { fillerPx.toDp() }))
            }
        }
    }
}