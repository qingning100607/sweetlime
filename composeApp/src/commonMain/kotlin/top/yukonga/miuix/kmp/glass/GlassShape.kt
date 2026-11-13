
package top.yukonga.miuix.kmp.glass

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Immutable
class GlassShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
    val smoothing: Float = 1f,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        val rect = Rect(0f, 0f, size.width, size.height)
        if (topStart + topEnd + bottomEnd + bottomStart == 0f) return Outline.Rectangle(rect)
        val isLtr = layoutDirection == LayoutDirection.Ltr
        return Outline.Rounded(
            RoundRect(
                rect = rect,
                topLeft = CornerRadius(if (isLtr) topStart else topEnd),
                topRight = CornerRadius(if (isLtr) topEnd else topStart),
                bottomRight = CornerRadius(if (isLtr) bottomEnd else bottomStart),
                bottomLeft = CornerRadius(if (isLtr) bottomStart else bottomEnd),
            ),
        )
    }

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): GlassShape = GlassShape(topStart, topEnd, bottomEnd, bottomStart, smoothing)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GlassShape) return false
        return topStart == other.topStart &&
            topEnd == other.topEnd &&
            bottomEnd == other.bottomEnd &&
            bottomStart == other.bottomStart &&
            smoothing == other.smoothing
    }

    override fun hashCode(): Int {
        var result = topStart.hashCode()
        result = 31 * result + topEnd.hashCode()
        result = 31 * result + bottomEnd.hashCode()
        result = 31 * result + bottomStart.hashCode()
        result = 31 * result + smoothing.hashCode()
        return result
    }

    override fun toString(): String = "GlassShape(topStart=$topStart, topEnd=$topEnd, bottomEnd=$bottomEnd, " +
        "bottomStart=$bottomStart, smoothing=$smoothing)"
}

fun GlassShape(cornerRadius: Dp, smoothing: Float = GlassDefaults.Smoothing): GlassShape = GlassShape(
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    CornerSize(cornerRadius),
    smoothing,
)

fun GlassShape(
    topStart: Dp,
    topEnd: Dp,
    bottomEnd: Dp,
    bottomStart: Dp,
    smoothing: Float = GlassDefaults.Smoothing,
): GlassShape = GlassShape(
    CornerSize(topStart),
    CornerSize(topEnd),
    CornerSize(bottomEnd),
    CornerSize(bottomStart),
    smoothing,
)
