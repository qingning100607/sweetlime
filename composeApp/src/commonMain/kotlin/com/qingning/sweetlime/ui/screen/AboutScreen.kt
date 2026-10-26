package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.APP_VERSION
import com.qingning.sweetlime.core.AUTHOR_NAME
import com.qingning.sweetlime.core.AUTHOR_QQ
import com.qingning.sweetlime.core.COMMUNITY_GROUP
import com.qingning.sweetlime.core.COMMUNITY_LINK
import com.qingning.sweetlime.core.REPO_URL
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.rememberLimeLogo
import com.qingning.sweetlime.ui.components.TiltPressCard
import com.qingning.sweetlime.ui.components.isInDarkTheme
import com.qingning.sweetlime.ui.effect.BgEffectBackground
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
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
    flowingStyle: Int = HyperOsStyle.AUTO,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val topBarScrollBehavior = MiuixScrollBehavior()
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var heroBottomPx by remember { mutableFloatStateOf(0f) }

    val density = LocalDensity.current
    val surface = MiuixTheme.colorScheme.surface
    val hazeState = remember { HazeState() }
    val backdrop = rememberLayerBackdrop()
    val shaderOk = remember { isRuntimeShaderSupported() }
    val isInDark = isInDarkTheme()
    val logoBlend = remember(isInDark) {
        if (isInDark) {
            listOf(
                BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab),
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab),
            )
        }
    }

    val scrollProgress by remember {
        derivedStateOf {
            val limit = heroBottomPx
            if (limit <= 0f) {
                0f
            } else {
                val index = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (index > 0) 1f else (offset.toFloat() / limit).coerceIn(0f, 1f)
            }
        }
    }

    BgEffectBackground(
        enabled = true,
        isOs3 = HyperOsStyle.resolveIsOs3(flowingStyle),
        modifier = modifier.fillMaxSize(),
        alpha = { 1f - scrollProgress },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop),
        ) {
            AboutHero(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topBarHeight + 60.dp)
                    .onGloballyPositioned {
                        heroBottomPx = it.positionInWindow().y + it.size.height
                    },
                progress = scrollProgress,
                backdrop = backdrop,
                blendColors = logoBlend,
                glassEnabled = shaderOk,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .hazeSource(state = hazeState)
                    .hyperScrollHaptic()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "hero_spacer") {
                    Spacer(modifier = Modifier.height(with(density) { heroBottomPx.toDp() }))
                }

                item(key = "project") {
                    SmallTitle(text = tr("关于项目"))
                    TiltPressCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        BasicComponent(
                            title = tr("GitHub 仓库"),
                            summary = "qingning100607/sweetlime",
                            onClick = { onOpenUrl(REPO_URL) },
                            endActions = {
                                Icon(
                                    imageVector = MiuixIcons.Basic.ArrowRight,
                                    contentDescription = null,
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
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            },
                        )
                    }
                }

                item(key = "author") {
                    SmallTitle(text = tr("关于作者"))
                    TiltPressCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        BasicComponent(
                            title = tr("作者"),
                            summary = trf("{} · QQ {}", AUTHOR_NAME, AUTHOR_QQ),
                            onClick = { onCopyText(AUTHOR_QQ, tr("作者 QQ")) },
                            endActions = {
                                Icon(
                                    imageVector = MiuixIcons.Copy,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            },
                        )
                        BasicComponent(
                            title = tr("交流群"),
                            summary = trf("QQ 群 {}", COMMUNITY_GROUP),
                            onClick = { onOpenUrl(COMMUNITY_LINK) },
                            endActions = {
                                Icon(
                                    imageVector = MiuixIcons.Basic.ArrowRight,
                                    contentDescription = null,
                                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                                )
                            },
                        )
                    }
                }

                item(key = "thanks") {
                    SmallTitle(text = tr("致谢"))
                    TiltPressCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        BasicComponent(title = "compose-miuix-ui")
                    }
                }

                item(key = "bottom_space") {
                    Spacer(modifier = Modifier.fillParentMaxHeight(0.55f))
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
                        .hazeEffect(
                            state = hazeState,
                            style = HazeStyle(
                                blurRadius = 20.dp,
                                noiseFactor = 0.15f,
                                tint = HazeTint(MiuixTheme.colorScheme.surface.copy(alpha = 0.16f)),
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
                    color = if (scrollProgress == 1f) surface else Color.Transparent,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(
                        alpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f),
                    ),
                    scrollBehavior = topBarScrollBehavior,
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
}

@Composable
private fun AboutHero(
    modifier: Modifier = Modifier,
    progress: Float,
    backdrop: Backdrop,
    blendColors: List<BlendColorEntry>,
    glassEnabled: Boolean,
) {
    val iconAlpha = 1f - ((progress - 0.70f) / 0.30f).coerceIn(0f, 1f)
    val nameAlpha = 1f - ((progress - 0.45f) / 0.25f).coerceIn(0f, 1f)
    val versionAlpha = 1f - ((progress - 0.20f) / 0.25f).coerceIn(0f, 1f)

    val glass = if (glassEnabled) {
        Modifier.textureBlur(
            backdrop = backdrop,
            shape = RoundedCornerShape(0.dp),
            blurRadius = 150f,
            colors = BlurColors(blendColors = blendColors),
            contentBlendMode = ComposeBlendMode.DstIn,
        )
    } else {
        Modifier
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = rememberLimeLogo(),
            contentDescription = null,
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    alpha = iconAlpha
                    val scale = 1f - (1f - iconAlpha) * 0.05f
                    scaleX = scale
                    scaleY = scale
                }
                .then(glass),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "SweetLime",
            style = MiuixTheme.textStyles.title1,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.onBackground,
            modifier = Modifier
                .graphicsLayer {
                    alpha = nameAlpha
                    val scale = 1f - (1f - nameAlpha) * 0.05f
                    scaleX = scale
                    scaleY = scale
                }
                .then(glass),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "v$APP_VERSION",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier
                .graphicsLayer { alpha = versionAlpha }
                .then(glass),
        )
    }
}
