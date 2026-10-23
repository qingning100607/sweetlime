package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.tools.QqNameColor
import com.qingning.sweetlime.ui.effect.SpringColumn
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * QQ 群昵称美化：给昵称上色 + 加消息尾巴。
 *
 * 功能与上游网页（`rwgtzs.github.io/qq-name-beautify`）一致，但规则是本地实现
 * （见 [QqNameColor]），离线可用，界面也和别的工具页统一。
 *
 * **「颜色」和「尾巴」是两个独立开关**：可以一起用，也可以只用其中一个。
 *
 * 版面顺序按「输入法友好」排：两个开关 → 昵称 / 尾巴输入框 → 颜色（预览 + 滑块）
 * → 复制 → 生成结果。输入框在上半屏，输入法弹出时不会把它们顶掉；
 * 颜色那一坨（预览条 + 四个滑块）最占地方，放下面。
 */
@Composable
internal fun QqNameToolScreen(onCopyText: (String, String) -> Unit) {
    var nickname by rememberSaveable { mutableStateOf("") }
    var tail by rememberSaveable { mutableStateOf("") }
    var useColor by rememberSaveable { mutableStateOf(true) }
    var useTail by rememberSaveable { mutableStateOf(true) }
    var alpha by rememberSaveable { mutableStateOf(QqNameColor.DEFAULT_ALPHA) }
    var red by rememberSaveable { mutableStateOf(QqNameColor.DEFAULT_RED) }
    var green by rememberSaveable { mutableStateOf(QqNameColor.DEFAULT_GREEN) }
    var blue by rememberSaveable { mutableStateOf(QqNameColor.DEFAULT_BLUE) }

    val previewColor = Color(red = red, green = green, blue = blue, alpha = alpha)
    // 尾巴算不算「有」：开关开着、而且不是纯空白（和 QqNameColor 内部判定保持一致）。
    val hasTail = useTail && tail.isNotBlank()
    val result = QqNameColor.build(
        nickname = nickname,
        tail = tail,
        alpha = alpha,
        red = red,
        green = green,
        blue = blue,
        useColor = useColor,
        useTail = useTail,
    )

    SpringColumn {
        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))

        // 两个部分的开关：颜色 / 尾巴互不影响。
        Card(modifier = Modifier.fillMaxWidth()) {
            SwitchPreference(
                title = tr("给昵称上色"),
                summary = tr("用下面的颜色给昵称染色，只在 QQ 群昵称里生效"),
                checked = useColor,
                onCheckedChange = { useColor = it },
            )
            SwitchPreference(
                title = tr("加消息尾巴"),
                summary = tr("在昵称后面接一句小尾巴，例如：喵~"),
                checked = useTail,
                onCheckedChange = { useTail = it },
            )
        }

        // 输入框放最上面：输入法弹起时它们是唯一必须一直看得见的两个控件。
        SmallTitle(text = tr("内容"))
        TextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = tr("昵称，例如：初音ミク"),
            useLabelAsPlaceholder = true,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth(),
        )
        if (useTail) {
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = tail,
                onValueChange = { tail = it },
                label = tr("消息尾巴，例如：喵~"),
                useLabelAsPlaceholder = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (useColor) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tr("别在 QQ 的账号昵称里用，它只在「群昵称」里生效。"),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        // 颜色放下面：预览条 + 四个滑块最占地方，不占输入框的位置。
        if (useColor) {
            SmallTitle(text = tr("颜色"))
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(previewColor),
                )
                Text(
                    text = trf(
                        "rgba({}, {}, {}, {})",
                        red.toString(),
                        green.toString(),
                        blue.toString(),
                        formatAlpha(alpha),
                    ),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    ChannelSlider(
                        label = tr("红色 Red"),
                        value = red,
                        onChange = { red = it },
                    )
                    ChannelSlider(
                        label = tr("绿色 Green"),
                        value = green,
                        onChange = { green = it },
                    )
                    ChannelSlider(
                        label = tr("蓝色 Blue"),
                        value = blue,
                        onChange = { blue = it },
                    )
                    ChannelSlider(
                        label = tr("透明度 Alpha"),
                        value = alpha,
                        onChange = { alpha = it },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        // 开了颜色时，昵称留空也能复制：这时复制的就是「颜色标记」本身，
        // 粘到群昵称的最前面就能给自己现在的昵称上色（不用把昵称再打一遍）。
        val canCopy = nickname.isNotBlank() || useColor
        TextButton(
            text = when {
                nickname.isNotBlank() -> tr("复制结果")
                useColor -> tr("复制颜色")
                else -> tr("先填昵称")
            },
            onClick = {
                onCopyText(
                    result,
                    if (nickname.isNotBlank()) tr("美化结果") else tr("颜色昵称"),
                )
            },
            enabled = canCopy,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        if (nickname.isBlank() && useColor) {
            Text(
                text = tr("点击单独复制颜色昵称，加在群昵称内即可"),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
        }

        SmallTitle(text = tr("生成结果"))
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = result,
                modifier = Modifier.padding(16.dp),
            )
            Text(
                text = trf("当前：{}，共{}个字符", modeLabel(useColor, hasTail), result.length.toString()),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

/** 当前到底生成的是哪几种东西 —— 结果卡里显示它。 */
private fun modeLabel(useColor: Boolean, hasTail: Boolean): String = when {
    useColor && hasTail -> tr("改颜色 + 加尾巴")
    useColor -> tr("只改颜色")
    hasTail -> tr("只加尾巴")
    else -> tr("只有昵称")
}

/** 一条通道滑块：上方「名称 …… 当前值」，下方滑块，范围 0..255（和上游一致）。 */
@Composable
private fun ChannelSlider(label: String, value: Int, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MiuixTheme.textStyles.footnote1)
            Text(text = value.toString(), style = MiuixTheme.textStyles.footnote1)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt().coerceIn(QqNameColor.CHANNEL_MIN, QqNameColor.CHANNEL_MAX)) },
            valueRange = QqNameColor.CHANNEL_MIN.toFloat()..QqNameColor.CHANNEL_MAX.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** 透明度按上游习惯显示成 0..255 的整数，这里只把它换算成 0..1 给预览用。 */
private fun formatAlpha(alpha: Int): String {
    val ratio = alpha.coerceIn(QqNameColor.CHANNEL_MIN, QqNameColor.CHANNEL_MAX) / 255f
    return ((ratio * 100).roundToInt() / 100f).toString()
}