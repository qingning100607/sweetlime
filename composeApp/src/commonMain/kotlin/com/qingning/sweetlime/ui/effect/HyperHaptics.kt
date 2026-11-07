package com.qingning.sweetlime.ui.effect
import android.os.SystemClock
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

import android.content.Context
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

object HyperHaptics {

    const val MIUI_HAPTIC_SLIDE: Int = 0x1000000D

    private val compatClass: Class<*>? by lazy {
        runCatching { Class.forName("miuix.view.HapticCompat") }.getOrNull()
    }

    private val compatMethod: java.lang.reflect.Method? by lazy {
        compatClass?.let { c ->
            runCatching {
                c.getDeclaredMethod("performHapticFeedbackAsync", View::class.java, Integer.TYPE)
                    .apply { isAccessible = true }
            }.getOrNull()
        }
    }

    var enabled: Boolean = true

    fun hasMiuixCompat(): Boolean = compatMethod != null

    fun hasRichHaptics(context: Context): Boolean = runCatching {

        val vibrator = (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
            ?.defaultVibrator
        vibrator?.areAllPrimitivesSupported(1, 7, 2) == true
    }.getOrDefault(false)

    fun performMiuixAsync(view: View, type: Int = MIUI_HAPTIC_SLIDE): Boolean {
        if (!enabled) return false
        val m = compatMethod ?: return false
        return runCatching {
            m.invoke(null, view, type)
            true
        }.getOrDefault(false)
    }

    fun performClick(view: View?): Boolean {
        if (!enabled) return false
        if (view == null) return false
        return runCatching { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
            .getOrDefault(false)
    }

    private fun constantFor(raw: Int): Int = when (raw) {
        16, 6, 13, 23, 3, 0, 17, 27, 26, 9, 22, 21, 1 -> raw
        else -> -1
    }

    internal fun performRaw(view: View?, raw: Int): Boolean {
        if (!enabled) return false
        if (view == null) return false
        val c = constantFor(raw)
        if (c == -1) return false
        return runCatching { view.performHapticFeedback(c) }.getOrDefault(false)
    }
}

private class HyperHapticFeedback(private val view: View) : HapticFeedback {

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {

        if (HyperHaptics.performMiuixAsync(view)) return

        HyperHaptics.performRaw(view, rawConstantOf(hapticFeedbackType))
    }
}

private fun rawConstantOf(type: HapticFeedbackType): Int = when (type) {
    HapticFeedbackType.VirtualKey -> HapticFeedbackConstants.VIRTUAL_KEY
    HapticFeedbackType.LongPress -> HapticFeedbackConstants.LONG_PRESS
    HapticFeedbackType.TextHandleMove -> HapticFeedbackConstants.TEXT_HANDLE_MOVE
    else -> HapticFeedbackConstants.CONTEXT_CLICK
}

@Composable
fun rememberHyperHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HyperHapticFeedback(view) }
}

@Composable
fun Modifier.hyperScrollHaptic(
    step: Dp = 40.dp,
    enabled: Boolean = true,
): Modifier {
    val view = LocalView.current
    val context = LocalContext.current
    val rich = remember(context) { HyperHaptics.hasRichHaptics(context) }
    if (!enabled) return this
    val stepPx = with(LocalDensity.current) { step.toPx() }
    val connection = remember(view, stepPx) { SlideHapticConnection(view, stepPx) }
    return this.nestedScroll(connection)
}

private class SlideHapticConnection(
    private val view: View,
    private val stepPx: Float,
) : NestedScrollConnection {

    private var armedTop = false
    private var armedBottom = false

    private var atEdge = false
    private var lastTickAt = 0L

    private fun tick(constant: Int) {
        if (!HyperHaptics.enabled) return
        val now = SystemClock.uptimeMillis()
        if (now - lastTickAt < 70L) return
        lastTickAt = now

        if (!HyperHaptics.performMiuixAsync(view)) {
            view.performHapticFeedback(constant)
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {

        if (available.y > 0f) {
            if (!armedTop) {
                armedTop = true
                tick(HapticFeedbackConstants.CLOCK_TICK)
            }
        } else if (available.y < 0f) {
            if (!armedBottom) {
                armedBottom = true
                tick(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        armedTop = false
        armedBottom = false
        return Velocity.Zero
    }
}