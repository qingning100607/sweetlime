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

/**
 * 流光动画的**更新频率上限**（帧/秒）。
 *
 * 为什么要限：这一层不是画一次就完事 —— 底层 1 次 + 页面根 1 次 + 顶栏 Haze 的采样源
 * 又 1 次，等于**每帧好几遍全屏着色器**（1440×3200 是 460 万像素）。而流光的运动本身
 * 极慢（配色几秒才过渡一次、光点缓慢漂移），逐帧刷新纯粹是白烧。
 *
 * 30fps 对这么慢的图案肉眼几乎看不出区别（MIUI 自己的流光也不跑满 60），
 * 但着色器的调用次数直接砍半。想恢复逐帧：把这个常数改成 60。
 */
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

        // 逐帧累加，而不是拿 (now - start) 直接算：
        //
        // 帧时钟会「停」—— 下拉通知栏、切 App、息屏，Choreographer 就不再发帧回调，
        // 这个循环卡在 withFrameNanos 上；回来时 now 一下跳了几百毫秒到几秒，
        // 直接相减的话流光图案会瞬间平移一大段，看起来就是「突然闪一下、像丢了帧」。
        // 所以每帧的步长做个上限（50ms ≈ 20fps），暂停期间的时间不再算进来，
        // 回来时接着原来的位置继续流动。
        val frameStep = 1f / FLOWING_ANIM_FPS
        var accumulated = startOffset
        var published = accumulated
        var last = start
        while (playing) {
            val now = withFrameNanos { it }
            val step = ((now - last) / 1_000_000_000f).coerceIn(0f, 0.05f)
            last = now
            accumulated += step
            // 攒够一帧的时长再往下发一次状态：着色器每帧重绘一次就够，多发的那些
            // 只会让整棵读这个 state 的页面跟着重组（见 FLOWING_ANIM_FPS 的说明）。
            if (accumulated - published >= frameStep) {
                published = accumulated
                time = accumulated
            }
        }
    }

    return { time }
}
