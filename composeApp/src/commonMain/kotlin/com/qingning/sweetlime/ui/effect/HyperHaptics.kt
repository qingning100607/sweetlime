package com.qingning.sweetlime.ui.effect

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicLong

private const val MIN_GAP_MS = 45L

private val lastTickAt = AtomicLong(0L)

private class VirtualKeyHapticFeedback(private val view: View) : HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        val now = SystemClock.uptimeMillis()
        val last = lastTickAt.get()
        if (now - last < MIN_GAP_MS) return
        if (!lastTickAt.compareAndSet(last, now)) return
        runCatching { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    }
}

@Composable
fun rememberHyperHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { VirtualKeyHapticFeedback(view) }
}

@Composable
fun Modifier.hyperScrollHaptic(
    step: Dp = 40.dp,
    enabled: Boolean = true,
): Modifier = this
