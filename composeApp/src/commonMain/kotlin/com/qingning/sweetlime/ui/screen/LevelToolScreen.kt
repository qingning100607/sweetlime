package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.rememberTilt
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import kotlin.math.abs
import kotlin.math.round

/** 气泡走到外沿对应的倾角：超过 15° 就贴边不动了。 */
private const val LEVEL_MAX_DEG = 15f

/** 判「已水平」的容差：±0.5°。 */
private const val LEVEL_TOLERANCE = 0.5f

/**
 * 水平仪：把手机平放，气泡居中就是水平。
 *
 * 读数来自加速度计（见 [rememberTilt]），这里只负责显示与校准：
 * 校准就是把「当前位置」记成 0°（比如桌子本身有点斜，先归零再看别处）。
 */
@Composable
internal fun LevelToolScreen() {
    val reading = rememberTilt()

    // 校准偏移：点「校准归零」时把当前读数记进来，之后显示的都是「相对校准点」的角度。
    var zeroX by rememberSaveable { mutableStateOf(0f) }
    var zeroY by rememberSaveable { mutableStateOf(0f) }
    var calibrated by rememberSaveable { mutableStateOf(false) }

    // 传感器是异步的：拿到第一帧读数前先显示「正在读取」，
    // 2.5 秒还没有就说明这台设备没有加速度计，给个明确说法。
    var gotReading by remember { mutableStateOf(false) }
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(reading) {
        if (reading != null) gotReading = true
    }
    LaunchedEffect(Unit) {
        delay(2500)
        timedOut = true
    }

    val x = (reading?.x ?: 0f) - zeroX
    val y = (reading?.y ?: 0f) - zeroY
    val level = reading != null && abs(x) < LEVEL_TOLERANCE && abs(y) < LEVEL_TOLERANCE

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),
        contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
    ) {
        item {
            Column {
                TopBarInsetSpacer()
                Spacer(modifier = Modifier.height(8.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        ReadOut(label = tr("左右"), degrees = x)
                        ReadOut(label = tr("前后"), degrees = y)
                        ReadOut(
                            label = tr("状态"),
                            text = if (level) tr("已水平") else tr("未水平"),
                            highlight = level,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                BubbleLevel(x = x, y = y, level = level)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        !gotReading && !timedOut -> tr("正在读取传感器…")
                        !gotReading -> tr("这台设备没有加速度计，水平仪用不了。")
                        else -> tr("气泡居中就是水平。倾斜越大气泡越靠外，外圈代表 ±15°。")
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(
                        text = tr("校准归零"),
                        onClick = {
                            zeroX = reading?.x ?: 0f
                            zeroY = reading?.y ?: 0f
                            calibrated = true
                        },
                        enabled = reading != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                    TextButton(
                        text = tr("清除校准"),
                        onClick = {
                            zeroX = 0f
                            zeroY = 0f
                            calibrated = false
                        },
                        enabled = calibrated,
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tr("桌面上先点一下「校准归零」，以后这个位置就显示 0°，量别的平面更准。"),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

/** 一格读数：上面是名字、下面是大号角度（或状态文案）。 */
@Composable
private fun ReadOut(label: String, degrees: Float? = null, text: String? = null, highlight: Boolean = false) {
    Column {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = text ?: formatDegrees(degrees ?: 0f),
            style = MiuixTheme.textStyles.title2,
            color = if (highlight) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceContainer,
        )
    }
}

/** 把角度格式化成「12.3°」。commonMain 里没有 String.format，所以自己算。 */
private fun formatDegrees(degrees: Float): String {
    val scaled = round(abs(degrees) * 10f).toInt()
    val sign = if (degrees < 0f) "-" else ""
    return "$sign${scaled / 10}.${scaled % 10}°"
}

/**
 * 气泡盘：外圈是 ±15° 的量程，中间小圈是「已水平」的判定范围，
 * 气泡跟着两个方向的倾角走，越倾斜越靠外。
 */
@Composable
private fun BubbleLevel(x: Float, y: Float, level: Boolean) {
    val plateColor = MiuixTheme.colorScheme.surfaceContainerHigh
    val lineColor = MiuixTheme.colorScheme.onSurfaceContainerVariant.copy(alpha = 0.28f)
    val targetColor = MiuixTheme.colorScheme.onSurfaceContainerVariant.copy(alpha = 0.45f)
    val bubbleColor = MiuixTheme.colorScheme.primary
    val glossColor = MiuixTheme.colorScheme.surface.copy(alpha = 0.5f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 32.dp),
    ) {
        val r = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // 盘面 + 外圈
        drawCircle(color = plateColor, radius = r, center = center)
        drawCircle(color = lineColor, radius = r, center = center, style = Stroke(width = 1.dp.toPx()))

        // 十字参考线
        val arm = r * 0.88f
        drawLine(lineColor, Offset(center.x - arm, center.y), Offset(center.x + arm, center.y), 1.dp.toPx())
        drawLine(lineColor, Offset(center.x, center.y - arm), Offset(center.x, center.y + arm), 1.dp.toPx())

        // 「已水平」的目标圈
        val bubbleR = r * 0.18f
        drawCircle(
            color = targetColor,
            radius = r * 0.15f,
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )

        // 气泡：倾角按量程映射成位移，超出就贴边（coerceIn 顺手做了这个限制）
        val travel = r - bubbleR - 6.dp.toPx()
        val bx = (x / LEVEL_MAX_DEG).coerceIn(-1f, 1f) * travel
        val by = (y / LEVEL_MAX_DEG).coerceIn(-1f, 1f) * travel
        val bubbleCenter = Offset(center.x + bx, center.y + by)
        drawCircle(color = bubbleColor, radius = bubbleR, center = bubbleCenter)
        // 一点高光，让气泡看着是「球」而不是色块
        drawCircle(
            color = glossColor,
            radius = bubbleR * 0.32f,
            center = Offset(bubbleCenter.x - bubbleR * 0.28f, bubbleCenter.y - bubbleR * 0.28f),
        )
        if (level) {
            // 水平时给气泡描一圈，屏幕上一眼能看出来
            drawCircle(
                color = bubbleColor.copy(alpha = 0.35f),
                radius = bubbleR * 1.35f,
                center = bubbleCenter,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}