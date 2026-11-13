
package top.yukonga.miuix.kmp.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke

fun Modifier.glassPanel(
    backdrop: Backdrop?,
    shape: GlassShape,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    material: GlassMaterial? = null,
    stroke: GlassStroke? = null,
    shadow: GlassShadow? = GlassShadows.Regular,
    shading: Boolean = true,
    fallback: Modifier = Modifier.clip(shape),
): Modifier = if (backdrop == null) {
    this
        .graphicsLayer { this.alpha = alpha }
        .glassShadow(shape, shadow)
        .then(fallback)
        .drawWithContent {
            drawContent()
            stroke?.let { drawGlassStroke(shape, layoutDirection, it, 1f) }
        }
} else {
    this
        .glassShadow(shape, shadow, alpha)
        .glass(
            backdrop = backdrop,
            shape = shape,
            style = style,
            alpha = alpha,
            material = material,
            stroke = stroke,
            shading = shading,
        )
}
