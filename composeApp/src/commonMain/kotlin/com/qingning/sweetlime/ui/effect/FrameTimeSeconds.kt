/*
 * 取自 lyricon（Apache License 2.0）
 *   https://github.com/kifranei/lyricon
 *   源路径：app/src/main/kotlin/io/github/proify/lyricon/app/compose/effect/FrameTimeSeconds.kt
 *
 * 按 Apache-2.0 第 4(b) 条在此声明修改：package 改为 com.qingning.sweetlime.ui.effect。
 * 除此之外内容与上游一致；原作者版权声明保留在本文件内（如有）。
 */
package com.qingning.sweetlime.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos

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

        while (playing) {
            val now = withFrameNanos { it }
            time = startOffset + (now - start) / 1_000_000_000f
        }
    }

    return { time }
}
