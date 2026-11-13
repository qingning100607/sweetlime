
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassStrokeLight(
    val x: Float,
    val y: Float,
    val z: Float,
    val color: Color,
)

@Immutable
data class GlassStroke(
    val width: Float,
    val bevel: Float,
    val color: Color,
    val primary: GlassStrokeLight,
    val secondary: GlassStrokeLight,
)

object GlassStrokes {

    @Stable
    val BigLight: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.1f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.6f)),
        secondary = GlassStrokeLight(0.5f, 0.9f, -0.5f, Color(1f, 1f, 1f, 0.05f)),
    )

    @Stable
    val MiddleLight: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.1f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.5f)),
        secondary = GlassStrokeLight(0.7f, 0.8f, 0f, Color(1f, 1f, 1f, 0.3f)),
    )

    @Stable
    val SmallLight: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.05f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.6f)),
        secondary = GlassStrokeLight(0.5f, 0.95f, -0.5f, Color(1f, 1f, 1f, 0.35f)),
    )

    @Stable
    val BigDark: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.1f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.4f)),
        secondary = GlassStrokeLight(0.5f, 0.9f, -0.5f, Color(1f, 1f, 1f, 0.01f)),
    )

    @Stable
    val MiddleDark: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.1f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.4f)),
        secondary = GlassStrokeLight(0.7f, 0.8f, 0f, Color(1f, 1f, 1f, 0.2f)),
    )

    @Stable
    val SmallDark: GlassStroke = GlassStroke(
        width = 0.8f,
        bevel = 1.2f,
        color = Color(1f, 1f, 1f, 0.05f),
        primary = GlassStrokeLight(0.2f, 0.5f, 0f, Color(1f, 1f, 1f, 0.6f)),
        secondary = GlassStrokeLight(0.5f, 0.95f, -0.36f, Color(1f, 1f, 1f, 0.25f)),
    )

    @Stable
    fun forTheme(
        isDark: Boolean,
        light: GlassStroke = MiddleLight,
        dark: GlassStroke = MiddleDark,
    ): GlassStroke = if (isDark) dark else light
}
