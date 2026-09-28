package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.tools.Base64Codec
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider


@Composable
internal fun Base64ToolScreen(onCopyText: (String, String) -> Unit) {
    var encodeMode by rememberSaveable { mutableStateOf(true) }
    ConverterToolScreen(
        placeholder = if (encodeMode) "输入要编码的文字，例如：你好世界" else "粘贴 Base64，例如：5L2g5aW9",
        hint = if (encodeMode) {
            "按 UTF-8 编码，输出标准 Base64（不足三位会补 =）。"
        } else {
            "忽略空格与换行，兼容 URL-safe 的 - 和 _，末尾的 = 也可以省略。"
        },
        resultLabel = if (encodeMode) "编码结果" else "解码结果",
        convert = { text ->
            if (encodeMode) {
                Base64Codec.encode(text)
            } else {
                Base64Codec.decode(text) ?: "不是合法的 Base64，请检查输入"
            }
        },
        onCopyText = onCopyText,
        extra = {
            Card(modifier = Modifier.fillMaxWidth()) {
                ModeRow(
                    title = "编码（文字 → Base64）",
                    selected = encodeMode,
                    onClick = { encodeMode = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ModeRow(
                    title = "解码（Base64 → 文字）",
                    selected = !encodeMode,
                    onClick = { encodeMode = false },
                )
            }
        },
    )
}

/* -------------------------------------------------------------------------- */
/* 2. 进制转换                                                                  */
/* -------------------------------------------------------------------------- */
