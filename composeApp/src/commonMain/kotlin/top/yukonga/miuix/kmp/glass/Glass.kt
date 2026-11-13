
package top.yukonga.miuix.kmp.glass

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.noiseDither
import top.yukonga.miuix.kmp.glass.internal.GLASS_COLOR_BLEND_SHADER_KEY
import top.yukonga.miuix.kmp.glass.internal.colorBlendEffect
import top.yukonga.miuix.kmp.glass.internal.drawGlassMask
import top.yukonga.miuix.kmp.glass.internal.drawGlassRim
import top.yukonga.miuix.kmp.glass.internal.drawGlassShadow
import top.yukonga.miuix.kmp.glass.internal.drawGlassStroke
import top.yukonga.miuix.kmp.glass.internal.glassEffect

fun Modifier.glass(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle = GlassDefaults.Style,
    alpha: Float = 1f,
    tint: Color = Color.Unspecified,
    material: GlassMaterial? = null,
    noiseCoefficient: Float = GlassDefaults.NoiseCoefficient,
    stroke: GlassStroke? = null,
    contentBlendMode: BlendMode = BlendMode.SrcOver,
    enabled: Boolean = true,
    shading: Boolean = true,
): Modifier = glassImpl(
    backdrop = backdrop,
    shape = shape,
    style = style,
    alpha = alpha,
    tint = tint,
    material = material,
    underlayMaterial = null,
    noiseCoefficient = noiseCoefficient,
    stroke = stroke,
    contentBlendMode = contentBlendMode,
    enabled = enabled,
    shading = shading,
)

internal fun Modifier.glassOnActionBar(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle,
    alpha: Float,
    material: GlassMaterial?,
    underlayMaterial: GlassMaterial,
    stroke: GlassStroke?,
    enabled: Boolean = true,
): Modifier = glassImpl(
    backdrop = backdrop,
    shape = shape,
    style = style,
    alpha = alpha,
    tint = Color.Unspecified,
    material = material,
    underlayMaterial = underlayMaterial,
    noiseCoefficient = GlassDefaults.NoiseCoefficient,
    stroke = stroke,
    contentBlendMode = BlendMode.SrcOver,
    enabled = enabled,
    shading = false,
)

private fun Modifier.glassImpl(
    backdrop: Backdrop,
    shape: GlassShape,
    style: GlassStyle,
    alpha: Float,
    tint: Color,
    material: GlassMaterial?,
    underlayMaterial: GlassMaterial?,
    noiseCoefficient: Float,
    stroke: GlassStroke?,
    contentBlendMode: BlendMode,
    enabled: Boolean,
    shading: Boolean,
): Modifier = this.drawBackdrop(
    backdrop = backdrop,
    shape = { shape },
    effects = {
        noiseDither(noiseCoefficient)
        val radius = material?.blurRadius?.value ?: (style.blur.small / GlassDefaults.SourceDensity)
        blur(radius.coerceIn(0f, BlurDefaults.MaxBlurRadius) * density)
        if (underlayMaterial != null) {
            colorBlendEffect(underlayMaterial, 1f, "${GLASS_COLOR_BLEND_SHADER_KEY}ActionBar")
        }
        if (material != null) colorBlendEffect(material, alpha)
        if (shading) glassEffect(style, shape, alpha, tint)
    },
    onDrawFront = {
        if (stroke != null) drawGlassStroke(shape, layoutDirection, stroke, alpha)
        drawGlassMask(shape, layoutDirection)
        if (shading) {
            drawGlassRim(shape, layoutDirection, style, if (tint.isSpecified) tint else style.inner.tint, alpha)
        }
    },
    contentBlendMode = contentBlendMode,
    enabled = enabled,
)

fun Modifier.glassShadow(
    shape: GlassShape,
    shadow: GlassShadow? = GlassShadows.Regular,
    alpha: Float = 1f,
): Modifier = glassShadow(shape, shadow) { alpha }

internal fun Modifier.glassShadow(
    shape: GlassShape,
    shadow: GlassShadow?,
    alpha: () -> Float,
): Modifier = if (shadow == null) {
    this
} else {
    this.drawBehind { drawGlassShadow(shape, layoutDirection, shadow, alpha()) }
}
