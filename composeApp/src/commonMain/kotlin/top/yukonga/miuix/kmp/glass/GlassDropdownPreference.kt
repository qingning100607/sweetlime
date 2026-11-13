
package top.yukonga.miuix.kmp.glass

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.DropdownArrowEndAction
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.PopupLayout

@Composable
fun GlassDropdownPreference(
    onSelectedIndexChange: (Int) -> Unit,
    items: List<String>,
    selectedIndex: Int,
    title: String,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showValue: Boolean = true,
    renderInRootScaffold: Boolean = true,
    titleColor: BasicComponentColors = BasicComponentDefaults.titleColor(),
    summary: String? = null,
    summaryColor: BasicComponentColors = BasicComponentDefaults.summaryColor(),
    startAction: @Composable (() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = BasicComponentDefaults.InsideMargin,
    sizing: GlassPopupSizing = GlassPopupSizing(),
    visuals: GlassPopupVisuals = GlassPopupDefaults.visuals(),
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val mounted = remember { mutableStateOf(false) }
    val anchor = rememberGlassPopupAnchor()
    var rowBounds by remember { mutableStateOf(Rect.Zero) }
    val actualEnabled = enabled && items.isNotEmpty()
    val currentOnExpandedChange by rememberUpdatedState(onExpandedChange)
    val setExpanded: (Boolean) -> Unit = remember {
        { next ->
            if (expanded != next) {
                expanded = next
                if (next) mounted.value = true
                currentOnExpandedChange?.invoke(next)
            }
        }
    }
    LaunchedEffect(actualEnabled) {
        if (!actualEnabled) setExpanded(false)
    }
    val hapticFeedback = LocalHapticFeedback.current
    val actionColor = if (actualEnabled) {
        MiuixTheme.colorScheme.onSurfaceVariantActions
    } else {
        MiuixTheme.colorScheme.disabledOnSecondaryVariant
    }

    BasicComponent(
        modifier = modifier.onGloballyPositioned { rowBounds = it.boundsInRoot() },
        title = title,
        titleColor = titleColor,
        summary = summary,
        summaryColor = summaryColor,
        startAction = startAction,
        endActions = {
            Row(
                modifier = Modifier.glassPopupAnchorValue(anchor),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val value = items.getOrNull(selectedIndex)
                if (showValue && !value.isNullOrEmpty()) {
                    Text(
                        text = value,
                        modifier = Modifier.padding(end = 8.dp).weight(1f, fill = false),
                        fontSize = MiuixTheme.textStyles.body2.fontSize,
                        color = actionColor,
                        textAlign = TextAlign.End,
                    )
                }
                DropdownArrowEndAction(actionColor = actionColor)
            }
        },
        bottomAction = bottomAction,
        insideMargin = insideMargin,
        onClick = {
            setExpanded(!expanded)
            if (expanded) hapticFeedback.performHapticFeedback(HapticFeedbackType.ContextClick)
        },
        role = Role.DropdownList,
        holdDownState = mounted.value,
        enabled = actualEnabled,
    )

    if (!mounted.value) return

    PopupLayout(
        visible = mounted,
        enableWindowDim = false,
        enableBackHandler = false,
        enterTransition = EnterTransition.None,
        exitTransition = ExitTransition.None,
        renderInRootScaffold = renderInRootScaffold,
    ) {
        var hostOrigin by remember { mutableStateOf(Offset.Zero) }
        Box(modifier = Modifier.fillMaxSize().onGloballyPositioned { hostOrigin = it.positionInRoot() }) {
            GlassDropdownPopupContent(
                show = expanded && actualEnabled,
                onDismissRequest = { setExpanded(false) },
                anchorBounds = rowBounds,
                backdrop = backdrop,
                anchor = anchor,
                sizing = sizing,
                visuals = visuals,
                coordinateOrigin = hostOrigin,
                onDismissFinished = { mounted.value = false },
            ) {
                items.forEachIndexed { index, item ->
                    GlassPopupItem(
                        text = item,
                        selected = selectedIndex == index,
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                            setExpanded(false)
                            onSelectedIndexChange(index)
                        },
                    )
                }
            }
        }
    }
}
