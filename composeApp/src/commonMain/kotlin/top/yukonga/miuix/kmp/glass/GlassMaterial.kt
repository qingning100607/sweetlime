
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

enum class GlassColorBlendMode {

    SrcOver,

    PlusDarker,

    PlusLighter,

    SoftLight,

    HardLight,

    Overlay,

    Luminosity,

    ColorDodge,

    ColorBurn,
}

@Immutable
data class GlassColorLayer(
    val color: Color,
    val mode: GlassColorBlendMode,
)

@Immutable
data class GlassMaterial(
    val blurRadius: Dp,
    val first: GlassColorLayer,
    val second: GlassColorLayer? = null,
    val third: GlassColorLayer? = null,
) {
    init {
        require(second != null || third == null) {
            "A GlassMaterial cannot carry a third colour layer without a second"
        }
    }
}

internal fun GlassMaterial.withUnderlayBlur(underlay: GlassMaterial?): GlassMaterial = if (underlay == null) {
    this
} else {
    copy(blurRadius = sqrt(blurRadius.value * blurRadius.value + underlay.blurRadius.value * underlay.blurRadius.value).dp)
}
