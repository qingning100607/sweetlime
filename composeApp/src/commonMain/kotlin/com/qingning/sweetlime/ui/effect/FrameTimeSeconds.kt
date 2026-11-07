
package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

private const val FLOWING_ANIM_FPS = 30

@Composable
fun rememberFrameTimeSeconds(
    playing: Boolean = true,
): () -> Float {
    var time by remember { mutableFloatStateOf(0f) }
    var startOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(playing) {
        if (!playing) {
            startOffset = time
            return@LaunchedEffect
        }

        val start = withFrameNanos { it }

        val frameStep = 1f / FLOWING_ANIM_FPS
        var accumulated = startOffset
        var published = accumulated
        var last = start
        while (playing) {
            val now = withFrameNanos { it }
            val step = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            accumulated += step

            if (accumulated - published >= frameStep) {
                published = accumulated
                time = accumulated
            }
        }
    }

    return { time }
}
