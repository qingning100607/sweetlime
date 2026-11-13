
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.max
import kotlin.math.min

@Stable
fun glassPressScale(shorterSidePx: Float, density: Float): Float = if (shorterSidePx <= 0f) {
    1f
} else {
    max((shorterSidePx - GlassMotion.PRESS_INSET_DP * density) / shorterSidePx, GlassMotion.PRESS_SCALE_MIN)
}

@Composable
fun Modifier.glassPress(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val density = LocalDensity.current.density
    var shorterSide by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = if (pressed) glassPressScale(shorterSide, density) else 1f,
        animationSpec = if (pressed) GlassMotion.pressDown() else GlassMotion.pressUp(),
        label = "glassPress",
    )

    return this
        .onSizeChanged { shorterSide = min(it.width, it.height).toFloat() }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}
