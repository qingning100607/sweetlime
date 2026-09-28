package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.tools.BmiCalculator
import com.qingning.sweetlime.core.tools.BmiLevel
import com.qingning.sweetlime.ui.components.TiltPressTextField
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme


@Composable
internal fun BmiToolScreen(onCopyText: (String, String) -> Unit) {
    var heightText by rememberSaveable { mutableStateOf("") }
    var weightText by rememberSaveable { mutableStateOf("") }
    val report = remember(heightText, weightText) {
        val height = BmiCalculator.parseNumber(heightText)
        val weight = BmiCalculator.parseNumber(weightText)
        if (height == null || weight == null) null else BmiCalculator.compute(height, weight)
    }
    val levelColor = when (report?.level) {
        BmiLevel.THIN -> Color(0xFF3B82F6)
        BmiLevel.NORMAL -> Color(0xFF22C55E)
        BmiLevel.OVERWEIGHT -> Color(0xFFF59E0B)
        BmiLevel.OBESE -> Color(0xFFEF4444)
        null -> MiuixTheme.colorScheme.onSurfaceContainerVariant
    }
    val summary = report?.let {
        "BMI ${it.bmiText}｜${it.level.label}\n健康体重范围：${it.healthyRangeText}"
    }.orEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TiltPressTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = "身高 cm",
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            TiltPressTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = "体重 kg",
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "分级采用中国成人标准：偏瘦 < 18.5，正常 18.5–23.9，超重 24–27.9，肥胖 ≥ 28。",
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        SmallTitle(text = "计算结果")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (report == null) {
                    Text(
                        text = if (heightText.isBlank() || weightText.isBlank()) {
                            "（填上身高和体重，这里会实时出结果）"
                        } else {
                            "数字看起来不太对，检查一下身高（50–260cm）和体重（0–500kg）。"
                        },
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = report.bmiText,
                            style = MiuixTheme.textStyles.title2,
                            color = levelColor,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = report.levelText,
                            style = MiuixTheme.textStyles.body2,
                            color = levelColor,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "健康体重范围：${report.healthyRangeText}",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceContainer,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.advice,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(
            text = "复制结果",
            onClick = { if (summary.isNotEmpty()) onCopyText(summary, "BMI") },
            enabled = summary.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.textButtonColorsPrimary(),
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}
