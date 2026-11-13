package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.rememberClickHaptic
import com.qingning.sweetlime.ui.effect.bleedHorizontal
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.APP_VERSION
import com.qingning.sweetlime.core.rememberLanguageIcon
import com.qingning.sweetlime.core.UpdateChecker
import com.qingning.sweetlime.data.SweetLimeSettings
import com.qingning.sweetlime.data.ThemeMode
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import com.qingning.sweetlime.core.i18n.AppLanguage
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Help
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Sidebar
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.ui.components.TiltPressCard

@Composable
fun SettingsScreen(
    settings: SweetLimeSettings,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,

    onOpenAbout: () -> Unit = {},

    updateResult: UpdateChecker.Result? = null,

    checkingUpdate: Boolean = false,

    onCheckUpdate: () -> Unit = {},

    onOpenDownload: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showThemePopup by remember { mutableStateOf(false) }
    var showBarStylePopup by remember { mutableStateOf(false) }
    var showFlowStylePopup by remember { mutableStateOf(false) }
    var showLanguagePopup by remember { mutableStateOf(false) }
    var topBarHeight by remember { mutableStateOf(0.dp) }

    val clickHaptic = rememberClickHaptic()
    val density = LocalDensity.current

    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .flowingPageBackground(MiuixTheme.colorScheme.surface),
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .pageBackdropLayer(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .hyperScrollHaptic(),

            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                Column {

            Spacer(modifier = Modifier.height(topBarHeight))

            SmallTitle(text = tr("外观"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                SwitchPreference(
                    title = tr("动态取色"),
                    summary = tr("跟随壁纸生成配色（Monet）"),
                    checked = settings.monet,
                    onCheckedChange = { settings.monet = it },
                    startAction = { RowStartIcon(MiuixIcons.Theme) },
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = tr("主题模式"),
                        onClick = {
 clickHaptic()
 showThemePopup = true
 },
                        startAction = { RowStartIcon(MiuixIcons.Hide) },
                        endActions = {
                            Text(
                                text = themeLabel(settings.themeMode),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowUpDown,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                    OverlayListPopup(
                        show = showThemePopup,
                        alignment = PopupPositionProvider.Align.End,
                        onDismissRequest = { showThemePopup = false },
                    ) {
                        ListPopupColumn {
                            DropdownImpl(
                                text = tr("跟随系统"),
                                optionSize = 3,
                                isSelected = settings.themeMode == ThemeMode.SYSTEM,
                                index = 0,
                                onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                    settings.themeMode = ThemeMode.SYSTEM
                                    showThemePopup = false
                                },
                            )
                            DropdownImpl(
                                text = tr("浅色"),
                                optionSize = 3,
                                isSelected = settings.themeMode == ThemeMode.LIGHT,
                                index = 1,
                                onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                    settings.themeMode = ThemeMode.LIGHT
                                    showThemePopup = false
                                },
                            )
                            DropdownImpl(
                                text = tr("深色"),
                                optionSize = 3,
                                isSelected = settings.themeMode == ThemeMode.DARK,
                                index = 2,
                                onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                    settings.themeMode = ThemeMode.DARK
                                    showThemePopup = false
                                },
                            )
                        }
                    }
                }
                SwitchPreference(
                    title = tr("悬浮底栏"),
                    checked = settings.floatingBottomBar,
                    onCheckedChange = { settings.floatingBottomBar = it },
                    startAction = { RowStartIcon(MiuixIcons.Sidebar) },
                )
                if (settings.floatingBottomBar) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = tr("底栏样式"),
                            onClick = {
                                clickHaptic()
                                showBarStylePopup = true
                            },
                            startAction = { RowStartIcon(MiuixIcons.Sidebar) },
                            endActions = {
                                Text(
                                    text = barStyleLabel(settings.floatingBarStyle),
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = MiuixIcons.Basic.ArrowUpDown,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            },
                        )
                        OverlayListPopup(
                            show = showBarStylePopup,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showBarStylePopup = false },
                        ) {
                            ListPopupColumn {
                                DropdownImpl(
                                    text = "iOS",
                                    optionSize = 2,
                                    isSelected = settings.floatingBarStyle == 0,
                                    index = 0,
                                    onSelectedIndexChange = {
                                        clickHaptic()
                                        settings.floatingBarStyle = 0
                                        showBarStylePopup = false
                                    },
                                )
                                DropdownImpl(
                                    text = "OS4",
                                    optionSize = 2,
                                    isSelected = settings.floatingBarStyle == 1,
                                    index = 1,
                                    onSelectedIndexChange = {
                                        clickHaptic()
                                        settings.floatingBarStyle = 1
                                        showBarStylePopup = false
                                    },
                                )
                            }
                        }
                    }
                }

                SwitchPreference(
                    title = tr("震动反馈"),
                    checked = settings.hapticFeedback,
                    onCheckedChange = { settings.hapticFeedback = it },
                    startAction = { RowStartIcon(MiuixIcons.Tune) },
                )

                SwitchPreference(
                    title = tr("流光背景"),
                    checked = settings.flowingBackground,
                    onCheckedChange = { settings.flowingBackground = it },
                    startAction = { RowStartIcon(MiuixIcons.Layers) },
                )

                if (settings.flowingBackground) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = tr("流光风格"),
                            onClick = {
 clickHaptic()
 showFlowStylePopup = true
 },
                            startAction = { RowStartIcon(MiuixIcons.Image) },
                            endActions = {
                                Text(
                                    text = flowStyleLabel(settings.flowingStyle),
                                    style = MiuixTheme.textStyles.body2,
                                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = MiuixIcons.Basic.ArrowUpDown,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            },
                        )
                        OverlayListPopup(
                            show = showFlowStylePopup,
                            alignment = PopupPositionProvider.Align.End,
                            onDismissRequest = { showFlowStylePopup = false },
                        ) {
                            ListPopupColumn {
                                DropdownImpl(
                                    text = tr("跟随系统"),
                                    optionSize = 3,
                                    isSelected = settings.flowingStyle == HyperOsStyle.AUTO,
                                    index = 0,
                                    onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                        settings.flowingStyle = HyperOsStyle.AUTO
                                        showFlowStylePopup = false
                                    },
                                )
                                DropdownImpl(
                                    text = "OS 2",
                                    optionSize = 3,
                                    isSelected = settings.flowingStyle == HyperOsStyle.OS2,
                                    index = 1,
                                    onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                        settings.flowingStyle = HyperOsStyle.OS2
                                        showFlowStylePopup = false
                                    },
                                )
                                DropdownImpl(
                                    text = "OS 3",
                                    optionSize = 3,
                                    isSelected = settings.flowingStyle == HyperOsStyle.OS3,
                                    index = 2,
                                    onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                        settings.flowingStyle = HyperOsStyle.OS3
                                        showFlowStylePopup = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            SmallTitle(text = tr("语言"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = tr("语言"),
                        onClick = {
 clickHaptic()
 showLanguagePopup = true
 },
                        startAction = { RowStartIcon(rememberLanguageIcon()) },
                        endActions = {
                            Text(

                                text = languageRowLabel(settings.language),
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowUpDown,
                                contentDescription = null,
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                    OverlayListPopup(
                        show = showLanguagePopup,
                        alignment = PopupPositionProvider.Align.End,
                        onDismissRequest = { showLanguagePopup = false },
                    ) {
                        ListPopupColumn {

                            val options: List<Pair<String, String>> = buildList {
                                add(AppLanguage.AUTO_CODE to tr("跟随系统"))
                                AppLanguage.entries.forEach { add(it.code to it.label) }
                            }
                            options.forEachIndexed { index, (code, label) ->
                                DropdownImpl(
                                    text = label,
                                    optionSize = options.size,

                                    isSelected = settings.language == code,
                                    index = index,
                                    onSelectedIndexChange = {
                                   clickHaptic()
                                    clickHaptic()
                                        settings.language = code
                                        showLanguagePopup = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            SmallTitle(text = tr("兼容性"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                BasicComponent(
                    title = tr("兼容性提示"),
                    startAction = { RowStartIcon(MiuixIcons.Help) },
                    summary = tr("花体、特殊符号等字符依赖系统字体，个别机型或 App 里可能显示成方框、问号，") +
                        tr("这是字体缺失导致的正常现象，换台设备或换成支持字体的 App 就能正常显示。") +
                        tr("另外不同平台（微信 / QQ / 游戏等）的昵称规则不一样，个别符号可能被过滤或截断，") +
                        tr("建议先复制到输入框里看一眼再保存。"),
                )
            }

            SmallTitle(text = tr("更新与协议"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {

                val newer = updateResult as? UpdateChecker.Result.Newer
                val updateSummary = when {
                    checkingUpdate -> tr("正在检查…")
                    newer != null -> trf("发现新版本 {}，点击前往 GitHub 下载", newer.latest)
                    updateResult is UpdateChecker.Result.UpToDate -> trf("已是最新版本 {}", APP_VERSION)
                    else -> tr("点击检查是否有新版本")
                }
                BasicComponent(
                    title = tr("检查更新"),
                    summary = updateSummary,
                    startAction = { RowStartIcon(MiuixIcons.Update) },
                    onClick = {
                        if (newer != null) onOpenDownload() else onCheckUpdate()
                    },
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Refresh,
                            contentDescription = tr("检查更新"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
                BasicComponent(
                    title = tr("隐私政策"),
                    onClick = onOpenPrivacy,
                    startAction = { RowStartIcon(MiuixIcons.Lock) },
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Info,
                            contentDescription = tr("打开隐私政策"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )

            }

            SmallTitle(text = tr("关于"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                BasicComponent(
                    title = tr("关于 SweetLime"),
                    onClick = onOpenAbout,
                    startAction = { RowStartIcon(MiuixIcons.Info) },
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Basic.ArrowRight,
                            contentDescription = tr("打开关于页"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
            }
            Spacer(modifier = Modifier.height(48.dp))

                }
            }
        }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bleedHorizontal()

                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        blurRadius = 20.dp,
                        noiseFactor = 0.15f,
                        tint = HazeTint(hazeTint.copy(alpha = if (flowing) 0.16f else 0.30f)),
                    ),
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 1f,
                        endIntensity = 0f,
                    )
                },
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to MiuixTheme.colorScheme.surface.copy(alpha = 0.55f),
                            0.6f to Color.Transparent,
                        ),
                    ),
            )
            SmallTopAppBar(
                title = tr("设置"),
                color = Color.Transparent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = tr("返回"),
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun RowStartIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

@Composable
private fun RowStartIcon(icon: Painter) {
    Icon(
        painter = icon,
        contentDescription = null,
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

private fun themeLabel(mode: Int): String = when (mode) {
    ThemeMode.LIGHT -> tr("浅色")
    ThemeMode.DARK -> tr("深色")
    else -> tr("跟随系统")
}

private fun languageRowLabel(code: String): String =
    if (code == AppLanguage.AUTO_CODE) tr("跟随系统") else AppLanguage.fromCode(code).label

private fun flowStyleLabel(style: Int): String = when (style) {
    HyperOsStyle.OS2 -> "OS 2"
    HyperOsStyle.OS3 -> "OS 3"
    else -> tr("跟随系统")
}


private fun barStyleLabel(style: Int): String = if (style == 1) "OS4" else "iOS"
