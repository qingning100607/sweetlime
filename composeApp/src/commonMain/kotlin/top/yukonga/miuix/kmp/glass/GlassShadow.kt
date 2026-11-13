
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassShadow(
    val color: Color,
    val offsetX: Float,
    val offsetY: Float,
    val radius: Float,
    val dispersion: Float,
)

object GlassShadows {

    private val Black = Color(0f, 0f, 0f, 0.05f)

    @Stable
    val Low: GlassShadow = GlassShadow(Black, 0f, 0f, 44f, 0.5f)

    @Stable
    val Regular: GlassShadow = GlassShadow(Black, 0f, 2f, 64f, 0.5f)

    @Stable
    val High: GlassShadow = GlassShadow(Black, 0f, 4f, 80f, 0.5f)

    @Stable
    val ExtraHigh: GlassShadow = GlassShadow(Black, 0f, 70f, 96f, 0.5f)

    @Stable
    val Float: GlassShadow = GlassShadow(Black, 0f, 0f, 24f, 0.5f)
}
