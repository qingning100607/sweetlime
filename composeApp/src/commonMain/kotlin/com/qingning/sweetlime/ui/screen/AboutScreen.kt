package com.qingning.sweetlime.ui.screen

import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.APP_VERSION
import com.qingning.sweetlime.core.AUTHOR_NAME
import com.qingning.sweetlime.core.AUTHOR_QQ
import com.qingning.sweetlime.core.COMMUNITY_GROUP
import com.qingning.sweetlime.core.COMMUNITY_LINK
import com.qingning.sweetlime.core.REPO_URL
import com.qingning.sweetlime.core.rememberLimeLogo
import com.qingning.sweetlime.ui.components.TiltPressCard
import com.qingning.sweetlime.ui.components.glassBar
import com.qingning.sweetlime.ui.effect.LocalFlowingBackground
import com.qingning.sweetlime.ui.effect.flowingPageLayer
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 「关于」页（二级页）。
 *
 * 排版照上游 lyricon 的 About 页来：顶部一整块居中的 **青柠 + 应用名 + 版本号**，
 * 下面按「关于项目 / 关于作者 / 致谢」分组成圆角卡片，正文一律沉在卡片里。
 *
 * 顶部那颗青柠用的是**暗色版**：只有青柠本身（不含绿底、不含白底方块），
 * 果肉与果皮都被压成暗色，见 `res/drawable-nodpi/lime.png`。
 *
 * ## 滚动行为（对齐上游 lyricon 的关于页）
 *
 * 这一页是**滚动驱动**的：`scrollProgress` 从 0（还在顶部）涨到 1（已经滑过顶部的
 * 青柠区），然后
 *
 *  * 整页那层流光按 `1 - scrollProgress` 淡出 —— 也就是说上滑之后流光会褪掉，
 *    露出底下的 `surface`（浅色主题下就是白底）；
 *  * 顶栏反过来按 `scrollProgress` 浮出同样的 `surface`。
 *
 * 没有开流光时行为和以前一样（顶栏铺玻璃），只是滚动时顶栏同样会跟着浮出来。
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    var topBarHeight by remember { mutableStateOf(0.dp) }
    //顶部青柠那一块的高度（px）：滚动进度就是拿它当分母算出来的。
    var heroSpanPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val backdrop = rememberLayerBackdrop()
    val blurPx = remember(density) { with(density) { 24.dp.toPx() } }
    val surface = MiuixTheme.colorScheme.surface
    val flowing = LocalFlowingBackground.current
    // 顶栏的实时模糊（Haze）：和主页/其他二级页同一套参数。
    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface

    val scrollProgress by remember {
        derivedStateOf {
            val span = heroSpanPx
            if (span <= 0) {
                0f
            } else {
                val index = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (index > 0) 1f else (offset.toFloat() / span).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(surface)
            // 流光跟着滚动淡出：滑过顶部的青柠区之后整页就是纯 surface，
            // 也就是上游 lyricon 那句 `BgEffectBackground(alpha = { 1f - scrollProgress })`。
            .flowingPageLayer { 1f - scrollProgress },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                // 内容层当顶栏实时模糊的采样源（列表从顶栏下面穿过去）。
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(top = topBarHeight, bottom = 24.dp),
        ) {
            item(key = "about_hero") {
                AboutHero(
                    modifier = Modifier.onSizeChanged { heroSpanPx = it.height },
                )
            }

            item(key = "about_project") {
                SmallTitle(text = tr("关于项目"))
                TiltPressCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    BasicComponent(
                        title = tr("GitHub 仓库"),
                        summary = tr("qingning100607/sweetlime · 点击打开源码页"),
                        onClick = { onOpenUrl(REPO_URL) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = tr("打开 GitHub 仓库"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    BasicComponent(
                        title = tr("开源许可"),
                        summary = tr("用到的开源项目与许可证"),
                        onClick = onOpenLicenses,
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Notes,
                                contentDescription = tr("打开开源许可"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                }
            }

            item(key = "about_author") {
                SmallTitle(text = tr("关于作者"))
                TiltPressCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    BasicComponent(
                        title = tr("作者"),
                        summary = trf("{} · QQ {} · 点击复制", AUTHOR_NAME, AUTHOR_QQ),
                        onClick = { onCopyText(AUTHOR_QQ, tr("作者 QQ")) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Copy,
                                contentDescription = tr("复制作者 QQ"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    BasicComponent(
                        title = tr("交流群"),
                        summary = trf("QQ 群 {} · 点击一键加群", COMMUNITY_GROUP),
                        onClick = { onOpenUrl(COMMUNITY_LINK) },
                        endActions = {
                            Icon(
                                imageVector = MiuixIcons.Basic.ArrowRight,
                                contentDescription = tr("打开加群链接"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        },
                    )
                }
            }

            item(key = "about_thanks") {
                SmallTitle(text = tr("致谢"))
                TiltPressCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    BasicComponent(title = "compose-miuix-ui（miuix）")
                }
            }

            // 列表末尾留白（上游 lyricon 同样这么干，取的是 0.72 个视口高）。
            // 关于页内容本来就短，不留这一截的话整页根本没有可滚动区间，
            // 上面那套「流光淡出 / 顶栏浮出」也就永远不会走到头。
            item(key = "about_bottom_space") {
                Spacer(modifier = Modifier.fillParentMaxHeight(0.55f))
            }
        }

        // 顶栏：停在顶部时全透明（流光直接透上来），一上滑就按进度浮出 surface，
        // 和上面流光的淡出正好对上 —— 滚过去之后就是干净的纯色顶栏。
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    // 顶栏实时模糊（Haze）：列表从下面滑过去时逐帧被糊。
                    // 停在顶部时按原设计整条透明（alpha = scrollProgress），一上滑就浮出来。
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
                        alpha = scrollProgress
                    },
            )
            SmallTopAppBar(
                title = tr("关于"),
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

/** 顶部居中那块：**暗色青柠** + 应用名 + 版本号。 */
@Composable
private fun AboutHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 只画那颗青柠本身（资源里已经是「暗色版」：不含绿底、不含白底方块）。
        Image(
            painter = rememberLimeLogo(),
            contentDescription = "SweetLime",
            modifier = Modifier.size(76.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "SweetLime",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v$APP_VERSION",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
        )
    }
}