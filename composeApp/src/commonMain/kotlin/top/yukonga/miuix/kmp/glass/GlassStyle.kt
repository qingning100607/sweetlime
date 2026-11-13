
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassStyle(
    val blend: GlassBlend,
    val inner: GlassInner,
    val edge: GlassEdge,
    val reflect: GlassReflect,
    val light: GlassLight,
    val refract: GlassRefract,
    val background: GlassBackground,
    val blur: GlassBlur,
)

@Immutable
data class GlassBlend(
    val curveA: Float,
    val curveB: Float,
    val curveC: Float,
    val curveD: Float,
    val amount: Float,
    val saturation: Float,
    val brightness: Float,
    val darker: Float,
    val darkerStart: Float,
    val darkerEnd: Float,
)

@Immutable
data class GlassInner(
    val bottom: Float,
    val tint: Color,
    val tintStrength: Float,
    val colorWhite: Float,
    val colorMix: Float,
    val colorPow: Float,
    val alpha: Float,
)

@Immutable
data class GlassEdge(
    val width: Float,
    val pow: Float,
    val thickness: Float,
    val reflectOffset: Float,
)

@Immutable
data class GlassReflect(
    val lighten: Float,
    val strength: Float,
)

@Immutable
data class GlassLight(
    val directionX: Float,
    val directionY: Float,
    val directionZ: Float,
    val intensity: Float,
    val oppositeIntensity: Float,
    val angleRange: Float,
)

@Immutable
data class GlassRefract(
    val ior: Float,
)

@Immutable
data class GlassBackground(
    val saturation: Float,
    val brightness: Float,
    val burn: Float,
    val unShade: Float,
)

@Immutable
data class GlassBlur(
    val small: Float,
    val big: Float,
)
