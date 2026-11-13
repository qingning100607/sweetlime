
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import top.yukonga.miuix.kmp.blur.Backdrop
import kotlin.math.roundToInt

@Stable
class GlassPopupAnchor {

    internal var secondaryBackProgressState: State<Float>? by mutableStateOf(null)

    internal var dropdownBackProgressState: State<Float>? by mutableStateOf(null)

    internal var dropdownRetainedValueAlpha: Float by mutableFloatStateOf(0f)

    val secondaryBackProgress: Float get() = secondaryBackProgressState?.value ?: 0f

    internal var surface: GlassAnchorSurface? by mutableStateOf(null)

    internal var popupVisuals: GlassPopupVisuals? by mutableStateOf(null)

    internal var surfaceProgress: State<Float>? by mutableStateOf(null)
    internal var surfaceOpacity: Float by mutableFloatStateOf(1f)
    internal val surfaceAlpha: Float get() = surfaceOpacity * (surfaceProgress?.value ?: 1f)
    internal var surfaceFloating: Boolean by mutableStateOf(false)

    internal var containerBounds: Rect by mutableStateOf(Rect.Zero)

    internal var contentBounds: Rect by mutableStateOf(Rect.Zero)

    internal var dropdownValueBounds: Rect? by mutableStateOf(null)

    internal var cornerRadius: Dp by mutableStateOf(0.dp)

    internal var floating: Boolean by mutableStateOf(false)

    internal var contentHidden: Boolean by mutableStateOf(false)

    internal var valueAlpha: Float by mutableFloatStateOf(1f)

    internal val dropdownValueAlpha: Float
        get() = (dropdownBackProgressState?.value ?: dropdownRetainedValueAlpha).coerceIn(0f, 1f)
}

internal data class GlassAnchorSurface(
    val backdrop: Backdrop?,
    val style: GlassStyle,
    val material: GlassMaterial,
    val underlayMaterial: GlassMaterial?,
    val stroke: GlassStroke?,
    val fill: Color,
)

internal data class GlassPopupAnchorElement(val anchor: GlassPopupAnchor) : Modifier.Element

@Composable
fun rememberGlassPopupAnchor(): GlassPopupAnchor {
    val anchor = remember { GlassPopupAnchor() }
    val alpha = animateFloatAsState(
        targetValue = if (anchor.contentHidden) 0f else 1f,
        animationSpec = GlassMotion.default(),
        label = "glassPopupAnchorValue",
    ).value
    SideEffect { anchor.valueAlpha = alpha }
    return anchor
}

@Stable
fun Modifier.glassPopupAnchor(
    anchor: GlassPopupAnchor,
    cornerRadius: Dp,
    floating: Boolean = false,
): Modifier = this
    .then(GlassPopupAnchorElement(anchor))
    .onGloballyPositioned {
        anchor.containerBounds = it.boundsInRoot()
        anchor.cornerRadius = cornerRadius
        anchor.floating = floating
    }
    .graphicsLayer { alpha = if (anchor.contentHidden) 0f else 1f }

@Stable
fun Modifier.glassPopupAnchorContent(anchor: GlassPopupAnchor): Modifier = this.onGloballyPositioned { anchor.contentBounds = it.boundsInRoot() }

@Stable
fun Modifier.glassPopupAnchorRow(anchor: GlassPopupAnchor): Modifier = this.onGloballyPositioned { anchor.containerBounds = it.boundsInRoot() }

@Stable
fun Modifier.glassPopupAnchorValue(anchor: GlassPopupAnchor): Modifier = this
    .onGloballyPositioned { anchor.dropdownValueBounds = it.boundsInRoot() }
    .graphicsLayer {
        alpha = maxOf(anchor.valueAlpha, anchor.dropdownValueAlpha)
    }

@Composable
fun BoxScope.GlassTransformPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchor: GlassPopupAnchor,
    backdrop: Backdrop?,
    anchorContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    simplified: Boolean = false,
    stacked: Boolean = false,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    anchorAlpha: Float = 1f,
    maskColor: Color = GlassPopupDefaults.maskColor(),
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
    gap: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    onMeasured: ((Size) -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val transition = updateTransition(show, label = "glassTransformPopup")
    val bounds = transition.animateFloat(
        transitionSpec = { GlassMotion.transformBounds(targetState) },
        label = "glassTransformBounds",
    ) { if (it) 1f else 0f }
    val center = transition.animateFloat(
        transitionSpec = { GlassMotion.transformCenter(targetState) },
        label = "glassTransformCenter",
    ) { if (it) 1f else 0f }
    val iconMaterial = transition.animateFloat(
        transitionSpec = { GlassMotion.transformIconMaterial(targetState) },
        label = "glassTransformIcon",
    ) { if (it) 1f else 0f }
    val contentMaterial = transition.animateFloat(
        transitionSpec = { GlassMotion.transformContentMaterial(targetState) },
        label = "glassTransformContent",
    ) { if (it) 1f else 0f }

    val pushedBack = animateFloatAsState(
        targetValue = if (stacked) 1f else 0f,
        animationSpec = GlassMotion.secondaryPopup(stacked),
        label = "glassTransformStacked",
    )
    val maskAlpha = animateFloatAsState(
        targetValue = if (stacked) 1f else 0f,
        animationSpec = GlassMotion.secondaryPopupMask(stacked),
        label = "glassTransformMask",
    )
    val active = show || transition.currentState || transition.isRunning
    val backProgress = rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show && !stacked,
        retainWhenInactive = false,
        resetSpec = GlassMotion.transformBounds(true),
        onDismissRequest = onDismissRequest,
    )
    SideEffect { anchor.popupVisuals = visuals }
    DisposableEffect(anchor) {
        onDispose { anchor.popupVisuals = null }
    }
    DisposableEffect(active, anchor) {
        anchor.contentHidden = active
        onDispose { anchor.contentHidden = false }
    }
    if (!active) return

    val anchorSurface = anchor.surface
    val anchorVisuals = anchorSurface?.let {
        visuals.copy(style = it.style, material = it.material, stroke = it.stroke, containerColor = it.fill, alpha = 1f)
    }
    val menuBackdrop = backdrop ?: anchorSurface?.backdrop
    val materialProgress = remember(bounds, backProgress) {
        derivedStateOf { popupFractionWithBack(bounds.value, backProgress.value).coerceIn(0f, 1f) }
    }
    val drawAnchorSurface by remember(anchorSurface, materialProgress) {
        derivedStateOf { anchorSurface != null && materialProgress.value < 1f }
    }
    val drawMenuSurface by remember(anchorSurface, materialProgress) {
        derivedStateOf { anchorSurface == null || materialProgress.value > 0f }
    }

    val startRect = anchor.containerBounds
    val layoutDirection = LocalLayoutDirection.current
    val iconRect = anchor.contentBounds.takeUnless { it.isEmpty } ?: startRect
    val startRadius = anchor.cornerRadius
    fun geometryProgress() = popupFractionWithBack(bounds.value, backProgress.value)
    fun centerProgress() = popupFractionWithBack(center.value, backProgress.value)
    fun iconProgress() = popupFractionWithBack(iconMaterial.value, backProgress.value)
    fun stackedProgress() = popupFractionWithBack(pushedBack.value, anchor.secondaryBackProgress)
    fun panelAlpha() = transformPanelAlpha(
        visualAlpha = visuals.alpha,
        floating = if (anchorSurface != null) anchor.surfaceFloating || anchor.surfaceAlpha > 0f else anchor.floating,

        anchorAlpha = if (anchorSurface != null) {
            if (visuals.alpha > 0f) anchor.surfaceAlpha / visuals.alpha else 0f
        } else {
            anchorAlpha
        },
        geometryProgress = geometryProgress(),
        iconMaterial = iconProgress(),
    )
    val travel = remember { TransformTravel() }

    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = menuBackdrop,
        sizing = sizing,
        visuals = visuals.copy(alpha = 1f),
        contentPadding = contentPadding,
        panelLayer = {
            alpha = panelAlpha()
            transformOrigin = TransformOrigin(
                if (startRect.center.x < travel.endCenterX) 0f else 1f,
                if (startRect.center.y < travel.endCenterY) 0f else 1f,
            )
            val s = 1f - 0.05f * stackedProgress()
            scaleX = s
            scaleY = s
        },
        overlay = {
            val fraction = popupFractionWithBack(maskAlpha.value, anchor.secondaryBackProgress).coerceIn(0f, 1f)
            if (fraction > GlassMotion.POPUP_MASK_MIN_VISIBLE_CHANGE) {
                drawRect(maskColor.copy(alpha = maskColor.alpha * fraction))
            }
        },
        frame = { end, page ->
            val frame = transformFrame(
                anchor = startRect,
                end = end,
                page = page,
                margin = sizing.safeMargin.toPx(),
                gap = gap.toPx(),
                sizeFraction = geometryProgress(),
                positionFraction = centerProgress(),
                startRadius = startRadius,
                endRadius = cornerRadius,
                layoutDirection = layoutDirection,
            )
            val settled = placeGlassPopup(
                startRect.translate(0f, gap.toPx()),
                end,
                sizing.safeMargin.toPx(),
                page,
                layoutDirection,
            )
            travel.endCenterX = settled.rect.center.x
            travel.endCenterY = settled.rect.center.y
            travel.endWidth = end.width
            frame
        },
        contentLayer = { end, _ ->
            val width = startRect.width + (end.width - startRect.width) * geometryProgress()
            val scale = if (end.width > 0f) (width / end.width).coerceAtLeast(0f) else 1f
            scaleX = scale
            scaleY = scale
            val fraction = popupFractionWithBack(contentMaterial.value, backProgress.value)
            alpha = fraction
            val blur = GlassMotion.TRANSFORM_BLUR_PX * (1f - fraction)
            renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
        },
        modifier = modifier,
        surface = { shape ->
            if (drawAnchorSurface && anchorSurface != null && anchorVisuals != null) {
                GlassPopupBackground(
                    backdrop = anchorSurface.backdrop,
                    shape = shape,
                    visuals = anchorVisuals,
                    underlayMaterial = anchorSurface.underlayMaterial,
                )
            }
            if (drawMenuSurface) {
                GlassPopupBackground(
                    backdrop = menuBackdrop,
                    shape = shape,
                    visuals = visuals.copy(alpha = 1f),
                    modifier = Modifier.graphicsLayer {
                        alpha = if (anchorSurface != null) materialProgress.value else 1f
                    },
                )
            }
        },
        onMeasured = onMeasured,
        interactive = isTransformPopupInteractive(show, stacked),
        scrollable = true,
        content = content,
    )

    val renderAnchorContent by remember(simplified, show, iconMaterial, backProgress) {
        derivedStateOf {
            shouldRenderAnchorContent(simplified, show && backProgress.value == 0f) &&
                popupFractionWithBack(iconMaterial.value, backProgress.value) < 0.999f
        }
    }
    if (renderAnchorContent) {
        Box(
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(
                        Constraints.fixed(
                            width = iconRect.width.roundToInt().coerceAtLeast(0),
                            height = iconRect.height.roundToInt().coerceAtLeast(0),
                        ),
                    )
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(iconRect.left.roundToInt(), iconRect.top.roundToInt())
                    }
                }
                .graphicsLayer {
                    translationX = (travel.endCenterX - startRect.center.x) * centerProgress()
                    translationY = (travel.endCenterY - startRect.center.y) * centerProgress()
                    val width = startRect.width + (travel.endWidth - startRect.width) * geometryProgress()
                    val growth = if (startRect.width > 0f) width / startRect.width else 1f
                    scaleX = growth
                    scaleY = growth
                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                    val fraction = iconProgress()
                    alpha = 1f - fraction
                    val blur = GlassMotion.TRANSFORM_BLUR_PX * fraction
                    renderEffect = if (blur > 0.5f) BlurEffect(blur, blur, TileMode.Decal) else null
                },
            content = { anchorContent() },
        )
    }
}

internal fun shouldRenderAnchorContent(simplified: Boolean, show: Boolean): Boolean = !simplified || !show

internal fun isTransformPopupInteractive(show: Boolean, stacked: Boolean): Boolean = show && !stacked

internal fun transformPanelAlpha(
    visualAlpha: Float,
    floating: Boolean,
    anchorAlpha: Float,
    geometryProgress: Float,
    iconMaterial: Float,
): Float = visualAlpha * if (floating) {
    lerp(anchorAlpha, 1f, geometryProgress.coerceIn(0f, 1f))
} else {
    iconMaterial
}

private fun transformFrame(
    anchor: Rect,
    end: Size,
    page: Size,
    margin: Float,
    gap: Float,
    sizeFraction: Float,
    positionFraction: Float,
    startRadius: Dp,
    endRadius: Dp,
    layoutDirection: androidx.compose.ui.unit.LayoutDirection,
): GlassPopupFrame {
    val placement = placeGlassPopup(anchor.translate(0f, gap), end, margin, page, layoutDirection)
    val width = anchor.width + (end.width - anchor.width) * sizeFraction
    val height = anchor.height + (end.height - anchor.height) * sizeFraction
    val centerX = anchor.center.x + (placement.rect.center.x - anchor.center.x) * positionFraction
    val centerY = anchor.center.y + (placement.rect.center.y - anchor.center.y) * positionFraction
    return GlassPopupFrame(
        rect = Rect(
            left = centerX - width / 2f,
            top = centerY - height / 2f,
            right = centerX + width / 2f,
            bottom = centerY + height / 2f,
        ),
        cornerRadius = startRadius + (endRadius - startRadius) * sizeFraction,
    )
}

private class TransformTravel {
    var endCenterX: Float = 0f
    var endCenterY: Float = 0f
    var endWidth: Float = 0f
}
