package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.tools.ContentDupCheck
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

@Composable
internal fun DupCheckToolScreen(onCopyText: (String, String) -> Unit) {
    var base by rememberSaveable { mutableStateOf("") }
    var target by rememberSaveable { mutableStateOf("") }
    val result = remember(base, target) {
        if (base.isBlank() || target.isBlank()) null else ContentDupCheck.compare(base, target)
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
,
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            Column {
                TopBarInsetSpacer()
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = base,
                    onValueChange = { base = it },
                    label = tr("第一段文字"),
                    useLabelAsPlaceholder = true,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = target,
                    onValueChange = { target = it },
                    label = tr("第二段文字"),
                    useLabelAsPlaceholder = true,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tr("只比较汉字、字母和数字，标点空格直接忽略；两段各取前 2000 字计算。"),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
                SmallTitle(text = tr("重复率"))
                if (result != null) {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Text(
                            text = trf("{}%", result.similarityPercent),
                            style = MiuixTheme.textStyles.title2,
                            color = MiuixTheme.colorScheme.onSurfaceContainer,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    Text(
                        text = trf("重复 {} 字 · 原文 {} 字 · 对比 {} 字", result.commonChars, result.baseChars, result.targetChars),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        text = tr("复制结果"),
                        onClick = {
                            val r = result ?: return@TextButton
                            onCopyText(
                                trf("重复率 {}% · 重复 {} 字", r.similarityPercent, r.commonChars),
                                tr("内容重复率"),
                            )
                        },
                        enabled = result != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}