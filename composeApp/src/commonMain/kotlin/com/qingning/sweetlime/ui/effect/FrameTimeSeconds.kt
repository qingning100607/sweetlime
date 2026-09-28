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

        // 逐帧累加，而不是拿 (now - start) 直接算：
        //
        // 帧时钟会「停」—— 下拉通知栏、切 App、息屏，Choreographer 就不再发帧回调，
        // 这个循环卡在 withFrameNanos 上；回来时 now 一下跳了几百毫秒到几秒，
        // 直接相减的话流光图案会瞬间平移一大段，看起来就是「突然闪一下、像丢了帧」。
        // 所以每帧的步长做个上限（50ms ≈ 20fps），暂停期间的时间不再算进来，
        // 回来时接着原来的位置继续流动。
        var accumulated = startOffset
        var last = start
        while (playing) {
            val now = withFrameNanos { it }
            val step = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            accumulated += step
            time = accumulated
        }
    }

    return { time }
}
