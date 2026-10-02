package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.ui.effect.flowingPageLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.qingning.sweetlime.ui.UpdateDownloadState
import com.qingning.sweetlime.ui.updateRowText
import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.AppLocale
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

/**
 * 设置页（二级页，由顶栏右上角入口进入）。
 *
 * 排版顺序（自上而下）：
 * 1. 外观；
 * 2. 语言；
 * 3. 兼容性提示；
 * 4. 更新与协议；
 * 5. 关于。
 *
 * 顶栏也铺了一层全分辨率玻璃，往上滚动内容时会从它下面滑过去。
 */
@Composable
fun SettingsScreen(
    settings: SweetLimeSettings,
    onBack: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenLicenses: () -> Unit,
    /** 打开独立的「关于」页（仓库 / 作者 / 交流群 / 致谢）。 */
    onOpenAbout: () -> Unit = {},
    /** 上一次检查更新的结果；由 App 持有，和主页横幅共用。 */
    updateResult: UpdateChecker.Result? = null,
    /** 正在检查更新。 */
    checkingUpdate: Boolean = false,
    /** 「下载 → 唤起安装器」的进度；和主页横幅共用同一份。 */
    updateDownload: UpdateDownloadState = UpdateDownloadState.Idle,
    /** 点「检查更新」：重新去更新源查一次。 */
    onCheckUpdate: () -> Unit = {},
    /** 点「检查更新」/ 主页横幅的统一入口：查 → 下载 → 安装。 */
    onUpdateAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showThemePopup by remember { mutableStateOf(false) }
    var showFlowStylePopup by remember { mutableStateOf(false) }
    var showLanguagePopup by remember { mutableStateOf(false) }
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    // 顶栏的「实时模糊」走 Haze：内容层是 source、顶栏是 effect，滚动时逐帧真高斯。
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        // 采样层单独一层、不参与滚动：和详情页/工具页同一套结构（解决下拉露灰条）。
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
                // 内容层既是要展示的东西，也当顶部实时模糊的采样源。
                // 采样源里先铺一层「和整页一样的底」：不然采样区是透明的，
                // 那片区域就等于没糊 —— 之前设置页顶栏看着比主页“淡”就是这个原因。
                
            contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
        ) {
            item {
                Column {

            // 空出顶栏高度，内容从玻璃下面开始；滚动时它从玻璃下面穿过去。
            Spacer(modifier = Modifier.height(topBarHeight))

            SmallTitle(text = tr("外观"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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
                        title = tr("深色模式"),
                        onClick = { showThemePopup = true },
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
                                    settings.themeMode = ThemeMode.DARK
                                    showThemePopup = false
                                },
                            )
                        }
                    }
                }
                // 悬浮底栏：直接照搬 KernelSU 那种浮在底部的小圆角胶囊。
                SwitchPreference(
                    title = tr("悬浮底栏"),
                    checked = settings.floatingBottomBar,
                    onCheckedChange = { settings.floatingBottomBar = it },
                    startAction = { RowStartIcon(MiuixIcons.Sidebar) },
                )
                // 震动反馈：控件点击震动 + 列表顶到边震动，全软件总开关。
                SwitchPreference(
                    title = tr("震动反馈"),
                    checked = settings.hapticFeedback,
                    onCheckedChange = { settings.hapticFeedback = it },
                    startAction = { RowStartIcon(MiuixIcons.Tune) },
                )
                // 流光背景：整屏一层着色器动效。默认关，且需 Android 13+，
                // 机型不支持时 BgEffectBackground 会自动退化成纯底色。
                SwitchPreference(
                    title = tr("流光背景"),
                    checked = settings.flowingBackground,
                    onCheckedChange = { settings.flowingBackground = it },
                    startAction = { RowStartIcon(MiuixIcons.Layers) },
                )
                // 流光风格：跟随系统 / OS2 / OS3。开关关着的时候不显示（没有流光就无所谓风格）。
                if (settings.flowingBackground) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        BasicComponent(
                            title = tr("流光风格"),
                            onClick = { showFlowStylePopup = true },
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
                                        settings.flowingStyle = HyperOsStyle.OS3
                                        showFlowStylePopup = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // 语言：中文 / 英文 / 繁体。词表见 core/i18n/Strings.kt，
            // 切换后靠 Compose 重组即时生效，不用重启 Activity。
            SmallTitle(text = tr("语言"))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    BasicComponent(
                        title = tr("语言"),
                        onClick = { showLanguagePopup = true },
                        startAction = { RowStartIcon(rememberLanguageIcon()) },
                        endActions = {
                            Text(
                                // 语言名一律用它自己的写法，不跟着翻译走
                                text = AppLocale.current.label,
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
                            AppLanguage.entries.forEachIndexed { index, language ->
                                DropdownImpl(
                                    text = language.label,
                                    optionSize = AppLanguage.entries.size,
                                    isSelected = AppLocale.current == language,
                                    index = index,
                                    onSelectedIndexChange = {
                                        settings.language = language.code
                                        showLanguagePopup = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // 兼容性提示：单独一张圆角卡，和上面的「外观」分开，也不再跟在关于信息后面。
            SmallTitle(text = tr("兼容性"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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

            // 更新与协议：检查更新 + 隐私政策 + 开源许可。
            SmallTitle(text = tr("更新与协议"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                // 第一次点：去查有没有新版；查到之后：在应用内下载并唤起系统安装器。
                // 文案由 updateRowText 统一推出来，下载过程中不会突然跳回"点击检查"。
                val updateSummary = updateRowText(
                    result = updateResult,
                    checking = checkingUpdate,
                    state = updateDownload,
                    currentVersion = APP_VERSION,
                )
                val hasNewer = updateResult is UpdateChecker.Result.Newer
                BasicComponent(
                    title = tr("检查更新"),
                    summary = updateSummary,
                    startAction = { RowStartIcon(MiuixIcons.Update) },
                    onClick = {
                        if (hasNewer) {
                            onUpdateAction()
                        } else {
                            onCheckUpdate()
                        }
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
                // 「开源许可」挪到「关于」页里去了（那边和仓库、作者放在一起），
                // 设置页不再重复一份。
            }

            // 关于：作者 / 交流群 / 仓库等信息全部收进独立的「关于」页（仿上游 lyricon），
            // 设置页这里只留一行入口，不再把四条信息摊在列表里。
            SmallTitle(text = tr("关于"))
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
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

        // 顶部玻璃栏：内容上滑时从它下面穿过去并被模糊。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    // 顶栏的实时模糊（Haze）：下面内容滚过去时逐帧被糊，
                // progressive 让「贴着最顶上最糊、往下渐隐到 0」。
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

/** 设置项左侧的小图标：和这一行的标题文字**同一个颜色**（onBackground），免得深浅不一。 */
@Composable
private fun RowStartIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MiuixTheme.colorScheme.onBackground,
    )
}

/** 同上，但图标是矢量资源（painter）而不是 Miuix 自带的 ImageVector。 */
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

/** 「流光风格」当前值：跟随系统 / OS 2 / OS 3。 */
private fun flowStyleLabel(style: Int): String = when (style) {
    HyperOsStyle.OS2 -> "OS 2"
    HyperOsStyle.OS3 -> "OS 3"
    else -> tr("跟随系统")
}
