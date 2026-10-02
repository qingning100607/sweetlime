package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.ui.effect.bounceVerticalScroll
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.core.i18n.tr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.tools.ColorConverter
import com.qingning.sweetlime.core.tools.ColorInfo
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
internal fun ColorToolScreen(onCopyText: (String, String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    val info = remember(input) { ColorConverter.parse(input) }
    val lines = if (info == null) emptyList() else colorLines(info)
    val report = lines.joinToString("\n") { "${it.first}：${it.second}" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .bounceVerticalScroll()
            .padding(horizontal = 12.dp),
    ) {
        // 顶栏是浮层：这点高度必须写在「滚动内容」里，内容才能滑到顶栏下面被实时糊。
        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = input,
            onValueChange = { input = it },
            label = tr("输入颜色，例如 #FF5722"),
            useLabelAsPlaceholder = true,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = tr("认得出 #RGB / #RRGGBB / #AARRGGBB、rgb()、rgba()、hsl()；") +
                tr("八位十六进制按 AARRGGBB 解析。"),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SmallTitle(text = tr("颜色预览"))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (info == null) {
                                MiuixTheme.colorScheme.onSurfaceContainerVariant.copy(alpha = 0.15f)
                            } else {
                                Color(info.red, info.green, info.blue, info.alpha)
                            },
                        ),
                )
                if (info == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (input.isBlank()) {
                            tr("（输入后这里会显示色块）")
                        } else {
                            tr("认不出这个颜色，换个写法试试。")
                        },
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
        }
        if (info != null) {
            SmallTitle(text = tr("各种写法"))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    lines.forEachIndexed { index, line ->
                        ColorLine(label = line.first, value = line.second)
                        if (index != lines.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            text = tr("复制全部"),
            onClick = { if (report.isNotEmpty()) onCopyText(report, tr("颜色代码")) },
            enabled = report.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

private fun colorLines(info: ColorInfo): List<Pair<String, String>> = listOf(
    "HEX" to info.hex,
    tr("HEX 带透明度") to info.hexAlpha,
    "RGB" to info.rgb,
    "RGBA" to info.rgba,
    "ARGB（Compose）" to info.argb,
    "HSL" to info.hsl,
    "HSV" to info.hsv,
    tr("透明度") to "${info.alphaPercent}%",
)

@Composable
private fun ColorLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.width(124.dp),
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceContainer,
        )
    }
}

/* -------------------------------------------------------------------------- */
/* 4. 汇率计算                                                                  */
/* -------------------------------------------------------------------------- */
