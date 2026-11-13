
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

internal data class NavigationDragTarget(
    val left: Float,
    val right: Float,
    val following: Boolean,
    val movingRight: Boolean,
) {
    val leftSpring: SpringSpec<Float>
        get() = if (following) GlassMotion.navDragFollow() else GlassMotion.edgeSpring(!movingRight)

    val rightSpring: SpringSpec<Float>
        get() = if (following) GlassMotion.navDragFollow() else GlassMotion.edgeSpring(movingRight)
}

internal fun navigationDragTarget(
    left: Float,
    width: Float,
    containerWidth: Float,
    delta: Float,
    changedItem: Boolean,
): NavigationDragTarget {
    val start = left.coerceIn(0f, (containerWidth - width).coerceAtLeast(0f))
    val end = start + width
    val trail = if (changedItem) 0f else (abs(delta) * 4f).coerceAtMost(60f)
    return NavigationDragTarget(
        left = if (delta > 0f && end < containerWidth) start - trail else start,
        right = if (delta < 0f && start > 0f) end + trail else end,
        following = !changedItem,
        movingRight = delta > 0f,
    )
}

internal fun navigationIndicatorBounds(left: Float, right: Float, containerWidth: Float, inset: Float): Offset {
    val min = inset.coerceIn(0f, containerWidth.coerceAtLeast(0f) / 2f)
    val max = (containerWidth - min).coerceAtLeast(min)
    val start = left.coerceIn(min, max)
    return Offset(start, right.coerceIn(start, max))
}
