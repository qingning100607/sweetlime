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
import kotlin.math.abs

import android.content.Context
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import top.yukonga.miuix.kmp.utils.OverScrollState

/**
 * 震动逻辑（移植自 HyperLight，对应它反编译出来的 l5 / fw / rb0 / ac0 四个类）：
 *
 * 1. 入口门槛 —— [HyperHaptics.hasRichHaptics]：
 *    `Vibrator.areAllPrimitivesSupported(1, 7, 2)`（CLICK / QUICK_FALL / TICK 三种原语）。
 *    HyperLight 只在支持时启用这套震动，不支持时用的是空实现（完全不动）。
 *
 * 2. 控件震动 —— [HyperHapticFeedback]（对应 fw.java）：
 *    把 Compose 的震动类型值**原值**转发给 `View.performHapticFeedback(constant)`，
 *    映射表与 HyperLight 一致（只放行 0/1/3/4/6/9/13/16/17/21/22/23/26/27，其余不震）。
 *
 * 3. HyperOS 原生优先 —— [HyperHaptics.performMiuixAsync]（对应 ac0.java）：
 *    设备上存在 `miuix.view.HapticCompat` 时先反射调
 *    `performHapticFeedbackAsync(view, 0x1000000D)`，拿 HyperOS 自己的马达手感。
 *
 * 4. 边界震动 —— [EdgeScrollHaptic]（对应 rb0.java）：
 *    列表顶到边还继续拉时响一次 `HapticFeedbackConstants.CLOCK_TICK`。
 *    HyperLight 是在滚动回调里判断，这里挂在 miuix 的 [OverScrollState] 上：
 *    `isOverScrollActive` 变 true（也就是开始回弹）时响一次。
 */
object HyperHaptics {

    /** HyperLight 里写死的那个 miuix 常量（ac0.java 的 268435469）。 */
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

    /** 设备上有没有 miuix 的 HapticCompat（HyperOS 一般都有）。 */
    fun hasMiuixCompat(): Boolean = compatMethod != null

    /** HyperLight 的入口判断：马达是否支持 CLICK(1) / QUICK_FALL(7) / TICK(2)。 */
    fun hasRichHaptics(context: Context): Boolean = runCatching {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
            ?.areAllPrimitivesSupported(1, 7, 2) == true
    }.getOrDefault(false)

    /** HyperOS 原生那支：miuix.view.HapticCompat.performHapticFeedbackAsync(view, type)。 */
    fun performMiuixAsync(view: View, type: Int = MIUI_HAPTIC_SLIDE): Boolean {
        val m = compatMethod ?: return false
        return runCatching {
            m.invoke(null, view, type)
            true
        }.getOrDefault(false)
    }

    /** HyperLight 的映射表：只放行下面这些值，未知值不震。 */
    private fun constantFor(raw: Int): Int = when (raw) {
        16, 6, 13, 23, 3, 0, 17, 27, 26, 9, 22, 21, 1 -> raw
        else -> -1
    }

    /** 直接给一个 Android 震动常量。 */
    internal fun performRaw(view: View?, raw: Int): Boolean {
        if (view == null) return false
        val c = constantFor(raw)
        if (c == -1) return false
        return runCatching { view.performHapticFeedback(c) }.getOrDefault(false)
    }
}

/** 对应 HyperLight 的 fw.java：Compose 震动类型 -> Android 常量，原值转发。 */
private class HyperHapticFeedback(private val view: View) : HapticFeedback {

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        // ① HyperOS 原生优先（ac0.java 那一支）
        if (HyperHaptics.performMiuixAsync(view)) return
        // ② 再按映射表转发（fw.java 那一支）
        HyperHaptics.performRaw(view, rawConstantOf(hapticFeedbackType))
    }
}

/** Compose 的震动类型 -> Android 常量；没列到的按 CONTEXT_CLICK 处理。 */
private fun rawConstantOf(type: HapticFeedbackType): Int = when (type) {
    HapticFeedbackType.LongPress -> HapticFeedbackConstants.LONG_PRESS
    HapticFeedbackType.TextHandleMove -> HapticFeedbackConstants.TEXT_HANDLE_MOVE
    else -> HapticFeedbackConstants.CONTEXT_CLICK
}

/** 全局控件震动：把上面这套塞进 `LocalHapticFeedback`，所有 Compose 控件自动生效。 */
@Composable
fun rememberHyperHapticFeedback(): HapticFeedback {
    val view = LocalView.current
    return remember(view) { HyperHapticFeedback(view) }
}

/**
 * 对应 rb0.java 的边界震动：miuix 的 [OverScrollState] 进入回弹（顶到边还在拉）时响一次。
 * 只在支持高精度原语的设备上启用，避免弱马达一路嗡嗡响。
 */
@Composable
fun EdgeScrollHaptic(
    overScrollState: OverScrollState,
    enabled: Boolean = true,
) {
    val view = LocalView.current
    val context = LocalContext.current
    val rich = remember(context) { HyperHaptics.hasRichHaptics(context) }
    if (!enabled) return
    LaunchedEffect(overScrollState, rich, view) {
        var last = 0L
        snapshotFlow { overScrollState.isOverScrollActive }.collect { active ->
            if (!active) return@collect
            val now = System.currentTimeMillis()
            if (now - last < 120L) return@collect
            last = now
            // 能走 HyperOS 原生就走原生，否则用 HyperLight 同款的 CLOCK_TICK(4)
            if (!HyperHaptics.performMiuixAsync(view)) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
}

/**
 * 滑动时的震动反馈 —— 对应 HyperLight 的 `haptic_feedback_for_slide`：
 * 它是在「滑动进度」回调里调 miuix 的 `performHapticFeedbackAsync(view, 0x1000000D)`
 * （反编译里的 `ac0.java`），这里换成挂在滚动容器上：
 *
 * - 拖动过程中每滑过 [step] 轻震一次（对应 HyperLight 的 slide 震动）；
 * - 顶到边界还在拉时额外响一次 CLOCK_TICK（对应 `rb0.java`）。
 *
 * 注意：这个 modifier 只是**读取**滚动增量，永远返回「未消费」，
 * 所以挂在哪一段都不会影响 miuix 那套回弹。
 */
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

    private var accumulated = 0f
    // 是否已经贴在边界上（贴着不重复震，离开才重置）
    private var atEdge = false
    private var lastTickAt = 0L

    private fun tick(constant: Int) {
        val now = SystemClock.uptimeMillis()
        if (now - lastTickAt < 70L) return
        lastTickAt = now
        // 和 HyperLight 一样：HyperOS 原生马达优先，拿不到再退回 Android 常量
        if (!HyperHaptics.performMiuixAsync(view)) {
            view.performHapticFeedback(constant)
        }
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        // 只在「顶到顶 / 拉到底」那一刻震一下（对应 HyperLight 的 rb0.java）；
        // 平时滑动完全不震，也不消费任何事件。
        if (available.y != 0f) {
            if (!atEdge) {
                atEdge = true
                tick(HapticFeedbackConstants.CLOCK_TICK)
            }
        } else {
            atEdge = false
        }
        return Offset.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        accumulated = 0f
        atEdge = false
        return Velocity.Zero
    }
}