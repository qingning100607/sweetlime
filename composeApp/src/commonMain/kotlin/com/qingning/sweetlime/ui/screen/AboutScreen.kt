package com.qingning.sweetlime.ui.screen
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic

import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    // 于是「关于」这个大标题跟着手指收起、往回滑又展开。
    val topAppBarScrollBehavior = MiuixScrollBehavior()
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
            .flowingPageLayer { 1f - scrollProgress },
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .hazeSource(state = hazeState)
                .hyperScrollHaptic()
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                // 内容层当顶栏实时模糊的采样源（列表从顶栏下面穿过去）。
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
                // 滑到底（scrollProgress == 1）那一瞬间换成 surface —— 浅色主题下就是白底。
                color = if (scrollProgress == 1f) surface else Color.Transparent,
                scrollBehavior = topAppBarScrollBehavior,
                // 标题同理：0.35 之前完全透明，0.35 → 1 之间渐显，滑到底就是清楚的白底 + 黑字。
                titleColor = MiuixTheme.colorScheme.onSurface.copy(
                    alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f),
                ),
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