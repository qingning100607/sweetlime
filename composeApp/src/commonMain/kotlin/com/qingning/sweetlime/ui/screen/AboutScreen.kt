package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingning.sweetlime.core.APP_VERSION
import com.qingning.sweetlime.core.AUTHOR_NAME
import com.qingning.sweetlime.core.AUTHOR_QQ
import com.qingning.sweetlime.core.COMMUNITY_GROUP
import com.qingning.sweetlime.core.COMMUNITY_LINK
import com.qingning.sweetlime.core.REPO_URL
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.rememberLimeLogo
import com.qingning.sweetlime.ui.components.tiltOnPress
import com.qingning.sweetlime.ui.effect.BgEffectBackground
import com.qingning.sweetlime.ui.effect.HyperOsStyle
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

private const val ICON_DETAIL_ALPHA = 0.45f
private val HERO_TOP_PADDING = 132.dp
private val HERO_BOTTOM_PADDING = 96.dp
private const val BOTTOM_SPACE_FRACTION = 0.72f
private val BOTTOM_SPACE_MIN = 160.dp

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenLicenses: () -> Unit,
    flowingStyle: Int = HyperOsStyle.AUTO,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var heroHeightPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val colorScheme = MiuixTheme.colorScheme
    val backdrop = rememberLayerBackdrop()

    val scrollProgress by remember {
        derivedStateOf {
            if (heroHeightPx <= 0) {
                0f
            } else {
                val index = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (index > 0) 1f else (offset.toFloat() / heroHeightPx).coerceIn(0f, 1f)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        BgEffectBackground(
            enabled = true,
            isOs3 = HyperOsStyle.resolveIsOs3(flowingStyle),
            modifier = Modifier.fillMaxSize(),
            bgModifier = Modifier.layerBackdrop(backdrop),
            alpha = { 1f - scrollProgress },
        ) {
            AboutContent(
                backdrop = backdrop,
                topBarHeight = topBarHeight,
                scrollProgress = scrollProgress,
                listState = listState,
                scrollBehavior = scrollBehavior,
                onHeroHeightChanged = { heroHeightPx = it },
                onOpenUrl = onOpenUrl,
                onCopyText = onCopyText,
                onOpenLicenses = onOpenLicenses,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } },
        ) {
            SmallTopAppBar(
                title = tr("关于"),
                scrollBehavior = scrollBehavior,
                color = colorScheme.surface.copy(alpha = scrollProgress),
                titleColor = colorScheme.onSurface.copy(alpha = scrollProgress),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = tr("返回"),
                            tint = colorScheme.onSurface,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun AboutContent(
    backdrop: LayerBackdrop,
    topBarHeight: androidx.compose.ui.unit.Dp,
    scrollProgress: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scrollBehavior: top.yukonga.miuix.kmp.basic.ScrollBehavior,
    onHeroHeightChanged: (Int) -> Unit,
    onOpenUrl: (String) -> Unit,
    onCopyText: (String, String) -> Unit,
    onOpenLicenses: () -> Unit,
) {
    val colorScheme = MiuixTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    val blurEnable = remember { isRuntimeShaderSupported() }
    val density = LocalDensity.current

    val titleBlend = remember(isDark) {
        if (isDark) {
            listOf(
                BlendColorEntry(Color(0xE6A1A1A1), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4DE6E6E6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xFF1AF500), BlurBlendMode.Lab),
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xCC4A4A4A), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xFF4F4F4F), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xFF1AF200), BlurBlendMode.Lab),
            )
        }
    }
    val cardBlend = remember(isDark) {
        if (isDark) {
            listOf(BlendColorEntry(Color(0x757A7A7A), BlurBlendMode.Luminosity))
        } else {
            listOf(
                BlendColorEntry(Color(0x340034F9), BlurBlendMode.Overlay),
                BlendColorEntry(Color(0xB3FFFFFF), BlurBlendMode.HardLight),
            )
        }
    }

    val heroLiftPx = with(density) { 96.dp.toPx() }
    var heroHeightDp by remember { mutableStateOf(220.dp) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = (1f - scrollProgress * 1.35f).coerceIn(0f, 1f)
                    translationY = -heroLiftPx * scrollProgress
                }
                .padding(top = topBarHeight + HERO_TOP_PADDING)
                .onSizeChanged { size ->
                    with(density) { heroHeightDp = size.height.toDp() }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 6.dp),
            ) {
                // 下面这层是流光色，上面再盖一层半透明原图，不然瓣会被抹平。
                if (blurEnable) {
                    Image(
                        painter = rememberLimeLogo(),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorScheme.onBackground),
                        modifier = Modifier
                            .fillMaxSize()
                            .textureBlur(
                                backdrop = backdrop,
                                shape = RoundedCornerShape(0.dp),
                                blurRadius = 150f,
                                colors = BlurColors(blendColors = titleBlend),
                                contentBlendMode = ComposeBlendMode.DstIn,
                            ),
                    )
                }
                Image(
                    painter = rememberLimeLogo(),
                    contentDescription = null,
                    alpha = if (blurEnable) ICON_DETAIL_ALPHA else 1f,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 5.dp)
                    .then(
                        if (blurEnable) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RoundedCornerShape(16.dp),
                                blurRadius = 150f,
                                colors = BlurColors(blendColors = titleBlend),
                                contentBlendMode = ComposeBlendMode.DstIn,
                            )
                        } else {
                            Modifier
                        },
                    ),
                text = "SweetLime",
                color = colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                color = colorScheme.onSurfaceContainerVariant,
                text = "v$APP_VERSION",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .overScrollVertical()
                .hyperScrollHaptic()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "hero_spacer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeightDp + HERO_TOP_PADDING + topBarHeight + HERO_BOTTOM_PADDING)
                        .onSizeChanged { onHeroHeightChanged(it.height) },
                )
            }

            item(key = "project") {
                Section {
                    SmallTitle(text = tr("关于项目"))
                    FrostedCard(backdrop, blurEnable, cardBlend, scrollProgress) {
                        BasicComponent(
                            title = tr("GitHub 仓库"),
                            summary = "qingning100607/sweetlime",
                            onClick = { onOpenUrl(REPO_URL) },
                        )
                        BasicComponent(
                            title = tr("开源许可"),
                            summary = tr("用到的开源项目与许可证"),
                            onClick = onOpenLicenses,
                        )
                    }
                }
            }

            item(key = "author") {
                Section {
                    SmallTitle(text = tr("关于作者"))
                    FrostedCard(backdrop, blurEnable, cardBlend, scrollProgress) {
                        BasicComponent(
                            title = tr("作者"),
                            summary = trf("{} · QQ {}", AUTHOR_NAME, AUTHOR_QQ),
                            onClick = { onCopyText(AUTHOR_QQ, tr("作者 QQ")) },
                        )
                        BasicComponent(
                            title = tr("交流群"),
                            summary = trf("QQ 群 {}", COMMUNITY_GROUP),
                            onClick = { onOpenUrl(COMMUNITY_LINK) },
                        )
                    }
                }
            }

            item(key = "thanks") {
                Section {
                    SmallTitle(text = tr("致谢"))
                    FrostedCard(backdrop, blurEnable, cardBlend, scrollProgress) {
                        BasicComponent(title = "compose-miuix-ui")
                    }
                }
            }

            item(key = "bottom_space") {
                Spacer(
                    modifier = Modifier
                        .fillParentMaxHeight(BOTTOM_SPACE_FRACTION)
                        .heightIn(min = BOTTOM_SPACE_MIN),
                )
            }
        }
    }
}

@Composable
private fun Section(content: @Composable ColumnScope.() -> Unit) {
    Column(content = content)
}

@Composable
private fun FrostedCard(
    backdrop: LayerBackdrop,
    blurEnable: Boolean,
    cardBlendColors: List<BlendColorEntry>,
    scrollProgress: Float,
    content: @Composable () -> Unit,
) {
    val colorScheme = MiuixTheme.colorScheme
    val isDark = colorScheme.background.luminance() < 0.5f
    // 倾斜包在玻璃外面，按下时整块（连玻璃）一起沉。
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .tiltOnPress(source),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (blurEnable) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(16.dp),
                            blurRadius = if (isDark) 72f else 64f,
                            colors = BlurColors(blendColors = cardBlendColors),
                        )
                    } else {
                        Modifier
                    },
                ),
            colors = CardDefaults.defaultColors(
                if (blurEnable) {
                    Color.Transparent
                } else if (isDark) {
                    Color(0xFF252528).copy(alpha = 0.86f + 0.08f * scrollProgress.coerceIn(0f, 1f))
                } else {
                    colorScheme.surfaceContainer
                },
                colorScheme.onSurface,
            ),
        ) {
            content()
        }
    }
}
