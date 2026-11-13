
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.flow.first
import top.yukonga.miuix.kmp.blur.Backdrop

@Composable
fun BoxScope.GlassSecondaryPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    materialAnchor: GlassPopupAnchor? = null,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    cornerRadius: Dp = GlassPopupDefaults.CornerRadius,
    contentPadding: PaddingValues = PaddingValues(vertical = GlassPopupDefaults.ContentPaddingVertical),
    onDismissFinished: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val state = remember { MutableTransitionState(false) }
    state.targetState = show
    val transition = rememberTransition(state, label = "glassSecondaryPopup")
    val progress = transition.animateFloat(
        transitionSpec = { GlassMotion.secondaryPopup(targetState) },
        label = "glassSecondaryBounds",
    ) { if (it) 1f else 0f }
    val active = show || state.currentState || !state.isIdle
    val backProgress = rememberGlassPopupBackProgress(
        show = show,
        active = active,
        enabled = show,
        retainWhenInactive = false,
        resetSpec = GlassMotion.secondaryPopup(true),
        onDismissRequest = onDismissRequest,
    )
    DisposableEffect(materialAnchor, backProgress) {
        materialAnchor?.secondaryBackProgressState = backProgress
        onDispose {
            if (materialAnchor?.secondaryBackProgressState === backProgress) {
                materialAnchor.secondaryBackProgressState = null
            }
        }
    }
    val onFinished by rememberUpdatedState(onDismissFinished)
    var hasOpened by remember { mutableStateOf(false) }
    LaunchedEffect(show) {
        if (show) {
            hasOpened = true
        } else if (hasOpened) {
            snapshotFlow { state.isIdle }.first { it }
            hasOpened = false
            onFinished?.invoke()
        }
    }
    if (!active) return
    val direction = LocalLayoutDirection.current
    val source = materialAnchor?.surface
    val resolvedVisuals = materialAnchor?.popupVisuals ?: visuals
    GlassPopupSurface(
        onDismissRequest = onDismissRequest,
        backdrop = backdrop ?: source?.backdrop,
        sizing = sizing,
        visuals = resolvedVisuals,
        contentPadding = contentPadding,
        panelLayer = {},
        overlay = {},
        frame = { end, page ->
            val settled = placeGlassSecondaryPopup(anchorBounds, end, sizing.safeMargin.toPx(), page, direction)
            GlassPopupFrame(
                secondaryPopupRect(
                    anchorBounds,
                    settled,
                    contentPadding.calculateTopPadding().toPx(),
                    contentPadding.calculateBottomPadding().toPx(),
                    popupFractionWithBack(progress.value, backProgress.value),
                ),
                cornerRadius,
            )
        },
        contentLayer = { _, _ -> },
        modifier = modifier,
        interactive = show,
        scrollable = true,
        content = content,
    )
}

internal fun placeGlassSecondaryPopup(anchor: Rect, size: Size, margin: Float, page: Size, direction: LayoutDirection): Rect {
    val width = size.width.coerceAtMost((page.width - margin * 2f).coerceAtLeast(0f))
    val height = size.height.coerceAtMost((page.height - margin * 2f).coerceAtLeast(0f))
    val x = if (direction == LayoutDirection.Ltr) anchor.left else anchor.right - width
    val left = x.coerceIn(margin, (page.width - margin - width).coerceAtLeast(margin))
    val top = anchor.top.coerceIn(margin, (page.height - margin - height).coerceAtLeast(margin))
    return Rect(left, top, left + width, top + height)
}

internal fun secondaryPopupRect(anchor: Rect, end: Rect, topPadding: Float, bottomPadding: Float, fraction: Float): Rect {
    val t = fraction.coerceIn(0f, 1f)
    return Rect(
        lerp(anchor.left, end.left, t),
        lerp(anchor.top - topPadding, end.top, t),
        lerp(anchor.right, end.right, t),
        lerp(anchor.bottom + bottomPadding, end.bottom, t),
    )
}
