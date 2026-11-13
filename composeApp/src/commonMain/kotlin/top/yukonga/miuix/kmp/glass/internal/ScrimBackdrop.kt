
package top.yukonga.miuix.kmp.glass.internal

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.Density
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BackdropEffectScope

@Stable
internal class ScrimBackdrop(
    private val backdrop: Backdrop,
    private val scrimColor: State<Color>,
    private val scale: State<Float>? = null,
) : Backdrop by backdrop {

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?,
        downscaleFactor: Int,
    ) {
        with(backdrop) {
            drawBackdrop(
                density = density,
                coordinates = coordinates,
                layerBlock = {
                    layerBlock?.invoke(this)

                    scale?.let {
                        scaleX *= it.value
                        scaleY *= it.value
                    }
                },
                downscaleFactor = downscaleFactor,
            )
        }
        val color = scrimColor.value
        if (color.alpha > 0f) {
            val padding = (density as? BackdropEffectScope)?.padding ?: 0f
            val scaledPadding = if (downscaleFactor > 1) {
                (padding / downscaleFactor).toInt().toFloat()
            } else {
                padding
            }

            drawRect(color, topLeft = Offset(-scaledPadding, -scaledPadding), size = size, blendMode = BlendMode.SrcAtop)
        }
    }
}
