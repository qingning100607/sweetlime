package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.tools.RadixConverter
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
internal fun RadixToolScreen(onCopyText: (String, String) -> Unit) {
    var fromBase by rememberSaveable { mutableStateOf(10) }
    ConverterToolScreen(
        placeholder = tr("输入要转换的数，例如：255"),
        hint = tr("支持 2–36 进制、任意位数，结果一次给全（二 / 八 / 十 / 十六进制）。"),
        resultLabel = tr("转换结果"),
        convert = { text ->
            val rows = RadixConverter.convertToCommon(text, fromBase)
            if (rows == null) {
                trf("不是合法的{}数字", RadixConverter.baseLabel(fromBase))
            } else {
                rows.joinToString("\n") { (label, value) -> "$label：$value" }
            }
        },
        onCopyText = onCopyText,
        extra = { BaseSelector(selected = fromBase, onSelect = { fromBase = it }) },
    )
}

/** 源进制选择：横向可滑的胶囊行（2–36 全给，滚一滚就能找到）。 */
@Composable
private fun BaseSelector(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        Text(
            text = tr("源进制"),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items((RadixConverter.MIN_BASE..RadixConverter.MAX_BASE).toList()) { base ->
                    val isSelected = base == selected
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) {
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable { onSelect(base) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "$base",
                            style = MiuixTheme.textStyles.body2,
                            color = if (isSelected) {
                                MiuixTheme.colorScheme.primary
                            } else {
                                MiuixTheme.colorScheme.onSurfaceContainerVariant
                            },
                            fontWeight = if (isSelected) FontWeight.Bold else null,
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/* 3. 颜色代码转换                                                              */
/* -------------------------------------------------------------------------- */
