package com.qingning.sweetlime.ui.screen

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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.qingning.sweetlime.ui.components.PressableRow
import com.qingning.sweetlime.ui.components.glassBar
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
import kotlinx.coroutines.launch

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
 * 3. 「关于」—— 作者 / 交流群信息放在最底部，并且压到 3 条以内。
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
    modifier: Modifier = Modifier,
) {
    var showThemePopup by remember { mutableStateOf(false) }
    var topBarHeight by remember { mutableStateOf(0.dp) }
    // 「检查更新」的临时状态：只在设置页里活着，离开页面就丢，不需要持久化。
    var checking by remember { mutableStateOf(false) }
    var updateResult by remember { mutableStateOf<UpdateChecker.Result?>(null) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop()
    val blurPx = remember(density) { with(density) { 22.dp.toPx() } }
    val glassTint = MiuixTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface),
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
            Card(
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
            }

            // 隐私 + 兼容性提示：单独一张圆角卡，和上面的「外观」分开，也不再跟在关于信息后面。
            SmallTitle(text = "隐私与兼容性")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                BasicComponent(
                    title = "隐私",
                    summary = "全部转换都在本机完成，不联网、不上传任何内容",
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
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
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                val newer = updateResult as? UpdateChecker.Result.Newer
                val updateSummary = when {
                    checking -> "正在检查…"
                    updateResult is UpdateChecker.Result.UpToDate -> "已是最新版本 $APP_VERSION"
                    updateResult is UpdateChecker.Result.NotConfigured -> "当前版本 $APP_VERSION（更新源未配置）"
                    newer != null ->
                        if (newer.releaseUrl != null) {
                            "发现新版本 ${newer.latest}，点击前往下载"
                        } else {
                            "发现新版本 ${newer.latest}，到交流群获取"
                        }
                    updateResult is UpdateChecker.Result.Failed -> "检查失败，请稍后再试"
                    else -> "点击检查是否有新版本"
                }
                PressableRow(
                    title = "检查更新",
                    summary = updateSummary,
                    onClick = {
                        // 已经查到新版本、也拿到了发布页，就直接跳过去；否则重新检查。
                        val releaseUrl = (updateResult as? UpdateChecker.Result.Newer)?.releaseUrl
                        if (releaseUrl != null) {
                            onOpenUrl(releaseUrl)
                        } else if (!checking) {
                            checking = true
                            scope.launch {
                                updateResult = UpdateChecker.check()
                                checking = false
                            }
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
            Card(
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
                    .glassBar(backdrop, blurPx, glassTint, fadeFromTop = true),
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
