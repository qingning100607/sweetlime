
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.BlurDefaults

object GlassDefaults {

    val CornerRadius: Dp = 24.dp

    val Smoothing: Float = 1f

    val SourceDensity: Float = 3f

    val NoiseCoefficient: Float = BlurDefaults.NoiseCoefficient

    val Overspill: Float = 3f

    @Stable
    val Style: GlassStyle = GlassStyles.CommonMediumRegularLowLight

    @Stable
    fun style(isDark: Boolean): GlassStyle = GlassStyles.forTheme(
        isDark = isDark,
        light = GlassStyles.CommonMediumRegularLowLight,
        dark = GlassStyles.CommonMediumRegularDark,
    )
}
