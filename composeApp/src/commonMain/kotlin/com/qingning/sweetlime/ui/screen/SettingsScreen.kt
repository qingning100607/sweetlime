package com.qingning.sweetlime.ui.screen

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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.APP_VERSION
import com.qingning.sweetlime.core.UpdateChecker
import com.qingning.sweetlime.data.SweetLimeSettings
import com.qingning.sweetlime.data.ThemeMode
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import com.qingning.sweetlime.ui.UpdateDownloadState
import com.qingning.sweetlime.ui.components.PressableRow
import com.qingning.sweetlime.ui.components.glassBar
import com.qingning.sweetlime.ui.updateRowText
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.ArrowUpDown
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import com.qingning.sweetlime.ui.components.TiltPressCard

/** 作者 / 交流群等信息（想改成自己的直接改这几个常量即可）。 */
private const val AUTHOR_NAME = "青柠不酸只甜"
private const val AUTHOR_QQ = "2892546640"
private const val COMMUNITY_GROUP = "948634109"
private const val COMMUNITY_LINK =
    "https://qun.qq.com/universal-share/share?ac=1&authKey=sMa6YUzkpV1kbRJ0YnvjD64JYC1umKbUQrz1gegRCY%2BOI8k4i%2BIAr4%2BMxUnB1H%2F%2B&busi_data=eyJncm91cENvZGUiOiI5NDg2MzQxMDkiLCJ0b2tlbiI6IlR0YTVCMkoxZmlpamk4QlIrMUdkdnpiTTkxd2pCUWVHR1dGcGRjR2hpWDJQMUxGZElaak9EclV6VldKU1Y5ZEIiLCJ1aW4iOiIyODkyNTQ2NjQwIn0%3D&data=hoiaXBFDEHpITCqTeUfXNAPFTgh3eruetmVD5xefVPOgjsKXMQD69-GUT2jJ3FroOeaVRForPWXhUcw6oVmchA&svctype=4&tempid=h5_group_info"

/**
 * 设置页（二级页，由顶栏右上角入口进入）。
 *
 * 排版顺序（自上而下）：
 * 1. 外观；
 * 2. 「隐私 + 兼容性」—— 单独一张圆角卡，和上面的设置项分开，不再吊在关于信息后面；
 * 3. 「关于」—— 作者 / 交流群 / GitHub 仓库，挂在页面最底部。
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
    var topBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop()
    val blurPx = remember(density) { with(density) { 22.dp.toPx() } }
    val glassTint = MiuixTheme.colorScheme.surface
    // 流光模式下顶栏不做玻璃：整页已经是「实流光」，再铺一层玻璃会在顶部留一块
    // 发白的横条（上游 lyricon 在流光时就是把 haze 整个关掉的）。
    val flowing = LocalFlowingBackground.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 内容层既是要展示的东西，也当顶部玻璃的采样源。
                .layerBackdrop(backdrop)
                .verticalScroll(rememberScrollState()),
        ) {
            // 空出顶栏高度，内容从玻璃下面开始；滚动时它从玻璃下面穿过去。
            Spacer(modifier = Modifier.height(topBarHeight))

            SmallTitle(text = "外观")
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                SwitchPreference(
                    title = "动态取色",
                    summary = "跟随壁纸生成配色（Monet）",
                    checked = settings.monet,
                    onCheckedChange = { settings.monet = it },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    PressableRow(
                        title = "深色模式",
                        onClick = { showThemePopup = true },
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
                                text = "跟随系统",
                                optionSize = 3,
                                isSelected = settings.themeMode == ThemeMode.SYSTEM,
                                index = 0,
                                onSelectedIndexChange = {
                                    settings.themeMode = ThemeMode.SYSTEM
                                    showThemePopup = false
                                },
                            )
                            DropdownImpl(
                                text = "浅色",
                                optionSize = 3,
                                isSelected = settings.themeMode == ThemeMode.LIGHT,
                                index = 1,
                                onSelectedIndexChange = {
                                    settings.themeMode = ThemeMode.LIGHT
                                    showThemePopup = false
                                },
                            )
                            DropdownImpl(
                                text = "深色",
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
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // 悬浮底栏：直接照搬 KernelSU 那种浮在底部的小圆角胶囊。
                SwitchPreference(
                    title = "悬浮底栏",
                    summary = "底部导航栏浮起来，变成一个小圆角胶囊",
                    checked = settings.floatingBottomBar,
                    onCheckedChange = { settings.floatingBottomBar = it },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                // 流光背景：整屏一层着色器动效。默认关，且需 Android 13+，
                // 机型不支持时 BgEffectBackground 会自动退化成纯底色。
                SwitchPreference(
                    title = "流光背景",
                    summary = "整屏铺一层缓慢流动的彩色光晕（HyperOS 那种观感）",
                    checked = settings.flowingBackground,
                    onCheckedChange = { settings.flowingBackground = it },
                )
                // 流光风格：跟随系统 / OS2 / OS3。开关关着的时候不显示（没有流光就无所谓风格）。
                if (settings.flowingBackground) {
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        PressableRow(
                            title = "流光风格",
                            summary = "「跟随系统」按 HyperOS 大版本自动选，也可以锁定 OS 2 / OS 3",
                            onClick = { showFlowStylePopup = true },
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
                                    text = "跟随系统",
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

            // 兼容性提示：单独一张圆角卡，和上面的「外观」分开，也不再跟在关于信息后面。
            SmallTitle(text = "兼容性")
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                BasicComponent(
                    title = "兼容性提示",
                    summary = "花体、特殊符号等字符依赖系统字体，个别机型或 App 里可能显示成方框、问号，" +
                        "这是字体缺失导致的正常现象，换台设备或换成支持字体的 App 就能正常显示。" +
                        "另外不同平台（微信 / QQ / 游戏等）的昵称规则不一样，个别符号可能被过滤或截断，" +
                        "建议先复制到输入框里看一眼再保存。",
                )
            }

            // 更新与协议：检查更新 + 隐私政策 + 开源许可。
            SmallTitle(text = "更新与协议")
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
                PressableRow(
                    title = "检查更新",
                    summary = updateSummary,
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
                            contentDescription = "检查更新",
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PressableRow(
                    title = "隐私政策",
                    summary = "本机处理，不联网、不上传任何内容",
                    onClick = onOpenPrivacy,
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Info,
                            contentDescription = "打开隐私政策",
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                PressableRow(
                    title = "开源许可",
                    summary = "用到的开源项目与许可证",
                    onClick = onOpenLicenses,
                    endActions = {
                        Icon(
                            imageVector = MiuixIcons.Notes,
                            contentDescription = "打开开源许可",
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    },
                )
            }

            // 关于：作者 / 交流群信息压到 3 条以内，并且放到页面最底部。
            SmallTitle(text = "关于")
            TiltPressCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                BasicComponent(
                    title = "SweetLime",
                    summary = "版本 $APP_VERSION",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    PressableRow(
                        title = "GitHub 仓库",
                        summary = "qingning100607/sweetlime · 点击打开源码页",
                        onClick = { onOpenUrl(UpdateChecker.REPO_URL) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = "打开 GitHub 仓库",
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    PressableRow(
                        title = "作者",
                        summary = "$AUTHOR_NAME · QQ $AUTHOR_QQ · 点击复制",
                        onClick = { onCopyText(AUTHOR_QQ, "作者 QQ") },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Copy,
                                contentDescription = "复制作者 QQ",
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    PressableRow(
                        title = "交流群",
                        summary = "QQ 群 $COMMUNITY_GROUP · 点击一键加群",
                        onClick = { onOpenUrl(COMMUNITY_LINK) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = "打开加群链接",
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
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
                    // 流光模式下不铺玻璃：顶部那一条直接露出页面自己的实流光。
                    .then(
                        if (flowing) {
                            Modifier
                        } else {
                            Modifier.glassBar(backdrop, blurPx, glassTint, fadeFromTop = true)
                        },
                    ),
            )
            SmallTopAppBar(
                title = "设置",
                color = Color.Transparent,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "返回",
                            tint = MiuixTheme.colorScheme.onBackground,
                        )
                    }
                },
            )
        }
    }
}

private fun themeLabel(mode: Int): String = when (mode) {
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
    else -> "跟随系统"
}

/** 「流光风格」当前值：跟随系统 / OS 2 / OS 3。 */
private fun flowStyleLabel(style: Int): String = when (style) {
    HyperOsStyle.OS2 -> "OS 2"
    HyperOsStyle.OS3 -> "OS 3"
    else -> "跟随系统"
}
