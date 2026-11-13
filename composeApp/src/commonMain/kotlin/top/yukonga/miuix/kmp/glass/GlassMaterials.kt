
package top.yukonga.miuix.kmp.glass

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object GlassMaterials {

    @Stable
    val PuredThinGlassLight: GlassMaterial = GlassMaterial(
        blurRadius = 20.dp,
        first = GlassColorLayer(Color(0x05000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.SoftLight),
        third = GlassColorLayer(Color(0x66FFFFFF), GlassColorBlendMode.HardLight),
    )

    @Stable
    val PuredThinGlassDark: GlassMaterial = GlassMaterial(
        blurRadius = 20.dp,
        first = GlassColorLayer(Color(0x1A000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x66565656), GlassColorBlendMode.Luminosity),
        third = GlassColorLayer(Color(0x993F3F3F), GlassColorBlendMode.Overlay),
    )

    @Stable
    val PopupViewGlassLight: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x05000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.SoftLight),
        third = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.HardLight),
    )

    @Stable
    val PopupViewGlassDark: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x1A000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x996C6C6C), GlassColorBlendMode.Luminosity),
        third = GlassColorLayer(Color(0x99060606), GlassColorBlendMode.SoftLight),
    )

    @Stable
    val DialogGlassLight: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x05000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.SoftLight),
        third = GlassColorLayer(Color(0x99FFFFFF), GlassColorBlendMode.HardLight),
    )

    @Stable
    val DialogGlassDark: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x1A000000), GlassColorBlendMode.PlusDarker),
        second = GlassColorLayer(Color(0x996C6C6C), GlassColorBlendMode.Luminosity),
        third = GlassColorLayer(Color(0x99060606), GlassColorBlendMode.SoftLight),
    )

    @Stable
    val ActionBarMaskLight: GlassMaterial = GlassMaterial(
        blurRadius = 40.dp,
        first = GlassColorLayer(Color(0x33F9F9F9), GlassColorBlendMode.Overlay),
        second = GlassColorLayer(Color(0xB3FFFFFF), GlassColorBlendMode.HardLight),
    )

    @Stable
    val ActionBarMaskDark: GlassMaterial = GlassMaterial(
        blurRadius = 60.dp,
        first = GlassColorLayer(Color(0x75000000), GlassColorBlendMode.ColorBurn),
        second = GlassColorLayer(Color(0x52000000), GlassColorBlendMode.SrcOver),
    )

    @Stable
    fun forTheme(isDark: Boolean, light: GlassMaterial, dark: GlassMaterial): GlassMaterial = if (isDark) dark else light

    @Stable
    fun puredThinGlass(isDark: Boolean): GlassMaterial = forTheme(isDark, PuredThinGlassLight, PuredThinGlassDark)

    @Stable
    fun popupViewGlass(isDark: Boolean): GlassMaterial = forTheme(isDark, PopupViewGlassLight, PopupViewGlassDark)

    @Stable
    fun dialogGlass(isDark: Boolean): GlassMaterial = forTheme(isDark, DialogGlassLight, DialogGlassDark)

    @Stable
    fun actionBarMask(isDark: Boolean): GlassMaterial = forTheme(isDark, ActionBarMaskLight, ActionBarMaskDark)
}
