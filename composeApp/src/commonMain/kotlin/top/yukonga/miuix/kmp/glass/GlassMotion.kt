
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.anim.folmeSpring

object GlassMotion {

    @Stable
    fun <T> springOf(damping: Float, response: Float): SpringSpec<T> {
        require(response > 0f) { "A spring's response must be greater than zero, was $response" }
        return folmeSpring(damping, response)
    }

    @Stable
    private fun transformSpring(damping: Float, response: Float): SpringSpec<Float> = folmeSpring(
        damping = damping,
        response = response,
        visibilityThreshold = TRANSFORM_VISIBILITY_THRESHOLD,
    )

    @Stable
    internal fun popupProgressSpring(spec: SpringSpec<Float>): SpringSpec<Float> = spring(
        dampingRatio = spec.dampingRatio,
        stiffness = spec.stiffness,
        visibilityThreshold = TRANSFORM_VISIBILITY_THRESHOLD,
    )

    @Stable
    fun <T> navIndicator(): SpringSpec<T> = springOf(0.7f, 0.4f)

    @Stable
    fun <T> navIndicatorTrail(): SpringSpec<T> = springOf(0.75f, 0.5f)

    @Stable
    fun <T> navDragFollow(): SpringSpec<T> = springOf(1f, 0.15f)

    @Stable
    fun <T> edgeSpring(leading: Boolean): SpringSpec<T> = if (leading) navIndicator() else navIndicatorTrail()

    @Stable
    fun navPressEnter(): SpringSpec<Color> = springOf(1f, 0.2f)

    @Stable
    fun navPressExit(): SpringSpec<Color> = springOf(0.95f, 0.35f)

    @Stable
    fun popupPressExit(): SpringSpec<Color> = springOf(1f, 0.15f)

    @Stable
    fun <T> navShowHide(): SpringSpec<T> = springOf(1f, 0.3f)

    @Stable
    fun <T> default(): SpringSpec<T> = springOf(0.95f, 0.35f)

    @Stable
    fun secondaryPopup(expanding: Boolean): SpringSpec<Float> = if (expanding) {
        transformSpring(0.95f, 0.35f)
    } else {
        transformSpring(0.95f, 0.2f)
    }

    @Stable
    internal fun secondaryPopupMask(expanding: Boolean): SpringSpec<Float> = folmeSpring(
        damping = 0.95f,
        response = if (expanding) 0.35f else 0.2f,
        visibilityThreshold = POPUP_MASK_MIN_VISIBLE_CHANGE * 0.75f,
    )

    internal const val POPUP_MASK_MIN_VISIBLE_CHANGE: Float = 1f / 256f

    @Stable
    fun <T> barExpand(): SpringSpec<T> = springOf(1f, 0.3f)

    @Stable
    fun <T> barCollapse(): SpringSpec<T> = springOf(1f, 0.15f)

    @Stable
    fun <T> barTrack(): SpringSpec<T> = springOf(1f, 0.6f)

    @Stable
    fun topBarButtonFloat(): FiniteAnimationSpec<Float> = tween(
        durationMillis = 350,
        easing = LinearEasing,
    )

    @Stable
    fun topBarMask(): FiniteAnimationSpec<Float> = tween(durationMillis = 100, easing = LinearEasing)

    @Stable
    fun <T> popupEnter(): SpringSpec<T> = springOf(0.8f, 0.28f)

    @Stable
    fun <T> popupExit(): SpringSpec<T> = springOf(0.95f, 0.2f)

    @Stable
    fun <T> dialogEnter(): SpringSpec<T> = springOf(0.95f, 0.35f)

    @Stable
    fun <T> dialogExit(): SpringSpec<T> = springOf(0.95f, 0.15f)

    @Stable
    fun <T> pressDown(): SpringSpec<T> = springOf(0.99f, 0.15f)

    @Stable
    fun <T> pressUp(): SpringSpec<T> = springOf(0.99f, 0.3f)

    @Stable
    fun <T> scaleIn(): SpringSpec<T> = springOf(0.6f, 0.35f)

    @Stable
    fun <T> scaleOut(): SpringSpec<T> = springOf(0.75f, 0.2f)

    @Stable
    fun navContent(): FiniteAnimationSpec<Color> = tween(
        durationMillis = 150,
        easing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f),
    )

    @Stable
    fun navContentFloat(): FiniteAnimationSpec<Float> = tween(
        durationMillis = 150,
        easing = CubicBezierEasing(0.33f, 0f, 0.67f, 1f),
    )

    @Stable
    fun fadeIn(): FiniteAnimationSpec<Float> = tween(durationMillis = 80, easing = LinearEasing)

    @Stable
    fun fadeOut(): FiniteAnimationSpec<Float> = tween(durationMillis = 150, easing = LinearEasing)

    @Stable
    fun popupMorph(): SpringSpec<Float> = springOf(0.82f, 0.33f)

    @Stable
    fun popupMorphFade(entering: Boolean): FiniteAnimationSpec<Float> = tween(durationMillis = if (entering) 200 else 150, easing = LinearEasing)

    @Stable
    fun popupMorphBlur(): FiniteAnimationSpec<Float> = tween(durationMillis = 200, easing = LinearEasing)

    @Stable
    fun transformBounds(entering: Boolean): SpringSpec<Float> = if (entering) {
        transformSpring(0.8f, 0.4f)
    } else {
        transformSpring(0.8f, 0.28f)
    }

    @Stable
    fun transformCenter(entering: Boolean): SpringSpec<Float> = if (entering) {
        transformSpring(0.8f, 0.25f)
    } else {
        transformSpring(0.8f, 0.4f)
    }

    @Stable
    fun transformIconMaterial(entering: Boolean): FiniteAnimationSpec<Float> = tween(
        durationMillis = TRANSFORM_MATERIAL_MS,
        delayMillis = if (entering) 0 else TRANSFORM_MATERIAL_DELAY_MS,
        easing = LinearEasing,
    )

    @Stable
    fun transformContentMaterial(entering: Boolean): FiniteAnimationSpec<Float> = tween(
        durationMillis = TRANSFORM_MATERIAL_MS,
        delayMillis = if (entering) TRANSFORM_MATERIAL_DELAY_MS else 0,
        easing = LinearEasing,
    )

    const val TRANSFORM_MATERIAL_MS: Int = 80

    const val TRANSFORM_MATERIAL_DELAY_MS: Int = 50

    const val TRANSFORM_BLUR_PX: Float = 50f

    internal const val TRANSFORM_VISIBILITY_THRESHOLD: Float = 0.0015f

    const val POPUP_START_WIDTH: Float = 0.15f

    const val POPUP_START_RATIO: Float = 0.2f

    const val POPUP_START_CORNER_DP: Float = 4f

    const val POPUP_MORPH_BLUR_PX: Float = 40f

    @Stable
    fun <T> arcBounds(entering: Boolean): SpringSpec<T> = if (entering) springOf(0.8f, 0.35f) else springOf(0.8f, 0.22f)

    @Stable
    fun <T> arcPosition(entering: Boolean): SpringSpec<T> = if (entering) springOf(0.8f, 0.22f) else springOf(0.8f, 0.35f)

    @Stable
    fun arcFade(entering: Boolean): FiniteAnimationSpec<Float> = tween(durationMillis = if (entering) 50 else 200, easing = LinearEasing)

    @Stable
    fun arcBlur(entering: Boolean): FiniteAnimationSpec<Float> = if (entering) snap() else tween(durationMillis = 200, easing = LinearEasing)

    const val ARC_START_WIDTH: Float = 0.69f

    const val ARC_START_RATIO: Float = 0.2f

    const val ARC_EXIT_BLUR_DP: Float = 30f

    const val ARC_VISIBLE_FRACTION: Float = 0.1f

    const val POPUP_ENTER_SCALE: Float = 0.2f

    const val POPUP_EXIT_SCALE: Float = 0.5f

    const val PRESS_SCALE_MIN: Float = 0.9f

    const val PRESS_INSET_DP: Float = 10f

    const val NAV_HIDE_SCALE: Float = 0.6f

    const val NAV_HIDE_BLUR_DP: Float = 18f

    const val NAV_SHOW_DELAY_MS: Long = 100
}
