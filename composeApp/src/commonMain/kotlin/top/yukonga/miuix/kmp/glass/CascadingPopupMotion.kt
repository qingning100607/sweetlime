
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.SpringSpec
import androidx.compose.ui.unit.LayoutDirection
import top.yukonga.miuix.kmp.anim.folmeSpring

private const val MAIN_SPRING_DAMPING = 0.95f
private const val EXPAND_SPRING_DAMPING = 0.99f
private const val EXPAND_SPRING_RESPONSE = 0.45f
private const val COLLAPSE_SPRING_RESPONSE = 0.2f
private const val ARROW_EXPAND_SPRING_RESPONSE = 0.2f
private const val ARROW_COLLAPSE_SPRING_RESPONSE = 0.3f
private const val PRIMARY_SHRUNK_SCALE = 0.95f

internal const val GLASS_ENTER_SCALE_FROM = 0.15f
internal const val GLASS_ENTER_SCALE_RANGE = 1f - GLASS_ENTER_SCALE_FROM

object CascadingPopupDefaults {

    val PrimaryShrunkScale: Float = PRIMARY_SHRUNK_SCALE

    fun arrowRotation(layoutDirection: LayoutDirection): Float = if (layoutDirection == LayoutDirection.Ltr) -90f else 90f

    fun <T> expandSpring(expanding: Boolean): SpringSpec<T> = if (expanding) {
        folmeSpring(EXPAND_SPRING_DAMPING, EXPAND_SPRING_RESPONSE)
    } else {
        folmeSpring(MAIN_SPRING_DAMPING, COLLAPSE_SPRING_RESPONSE)
    }

    fun <T> arrowSpring(expanding: Boolean): SpringSpec<T> = folmeSpring(
        damping = MAIN_SPRING_DAMPING,
        response = if (expanding) ARROW_EXPAND_SPRING_RESPONSE else ARROW_COLLAPSE_SPRING_RESPONSE,
    )
}
