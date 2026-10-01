package com.qingning.sweetlime.ui.screen

import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.qingning.sweetlime.ui.effect.pageBackdropLayer
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.ui.effect.flowingPageLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.readClipboard
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Clear
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 详情页：一个样式 + 一段原文 = 一个结果。
 *
 * **原文是能改的。** 从主页、分类、搜索、收藏、最近使用 —— 不管从哪进来，
 * 「原文」那一栏都是一个可编辑的输入框，边改边出效果：
 * 点「最近使用」进去就是想图个方便，进来还能顺手换个字看效果，不然点进去干嘛。
 *
 * 初始原文由调用方给（主页输入框 / 搜索框里那行字），改动时通过 [onInputChange]
 * 回写给上层，所以返回后主页输入框里也是你刚改的那段字，不会前后不一致。
 *
 * @param transform 当前样式。
 * @param initialInput 初始原文。
 * @param isFavorite 判断「当前这段原文 + 这个样式」有没有被收藏。
 * @param onToggleFavorite 收藏 / 取消收藏当前这条。
 * @param onInputChange 原文改动时回写（同步到主页输入框）。
 * @param onCopy 复制当前效果。
 * @param onShare 分享当前效果。
 */
@Composable
fun DetailScreen(
    transform: TextTransform,
    initialInput: String,
    isFavorite: (String) -> Boolean,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onInputChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 本地可编辑原文：用样式 id 作 key，换样式时重置为该样式带进来的原文。
    var text by rememberSaveable(transform.id) { mutableStateOf(initialInput) }
    val output = remember(text) { transform.transform(text) }
    val hasEffect = output != text && text.isNotEmpty()
    val favorite = isFavorite(text)

    val hazeState = remember { HazeState() }
    val hazeTint = MiuixTheme.colorScheme.surface
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiuixTheme.colorScheme.surface).flowingPageLayer(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 采样源：在顶栏这一条里铺一层和整页一模一样的底（流光 / 纯色），
            // 顶栏的模糊就糊它 —— 和主页顶栏同一套参数，观感一致。
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .pageBackdropLayer()
                    .hazeSource(state = hazeState),
            ) {}
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .hazeEffect(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 20.dp,
                            noiseFactor = 0.15f,
                            tint = HazeTint(hazeTint.copy(alpha = 0.30f)),
                        ),
                    ) {
                        progressive = HazeProgressive.verticalGradient(
                            startIntensity = 1f,
                            endIntensity = 0f,
                        )
                    },
            )
            SmallTopAppBar(
            // 顶栏自己不铺底：二级页的背景已经是「实流光」了，铺底会把顶部那块盖成纯白。
            color = Color.Transparent,
            title = transform.title,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
        ) {
            SmallTitle(text = tr("原文"))
            Card(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onInputChange(it)
                    },
                    label = tr("输入要转换的文字"),
                    useLabelAsPlaceholder = true,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                // 跟主页一样的一排小工具：字数 + 粘贴 + 清空。
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = trf("{} 字", text.length),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            readClipboard()?.let {
                                text = it
                                onInputChange(it)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Paste,
                            contentDescription = tr("粘贴"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                text = ""
                                onInputChange("")
                            },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Clear,
                                contentDescription = tr("清空"),
                                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                }
            }

            SmallTitle(text = tr("效果"))
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = when {
                        text.isEmpty() -> tr("上面输入文字，这里就是转换后的效果。")
                        hasEffect -> output
                        else -> tr("这段文字在这个样式里没有变化。")
                    },
                    style = MiuixTheme.textStyles.title2,
                    color = if (hasEffect) {
                        MiuixTheme.colorScheme.onSurfaceContainer
                    } else {
                        MiuixTheme.colorScheme.onSurfaceContainerVariant
                    },
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = tr("复制"),
                    onClick = { onCopy(output) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
                TextButton(
                    text = tr("分享"),
                    onClick = { onShare(output) },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = if (favorite) tr("取消收藏") else tr("收藏"),
                    onClick = { onToggleFavorite(text) },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}