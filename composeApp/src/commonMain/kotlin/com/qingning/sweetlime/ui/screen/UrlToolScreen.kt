package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.tools.UrlCodec
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider

/**
 * URL 编解码（工具箱里 id = `url`）。
 *
 * 布局和 Base64 那个一模一样：上面一个「模式」卡，下面输入 → 实时出结果。
 */
@Composable
internal fun UrlToolScreen(onCopyText: (String, String) -> Unit) {
    var encodeMode by rememberSaveable { mutableStateOf(true) }
    ConverterToolScreen(
        placeholder = if (encodeMode) {
            "输入要编码的文字或链接，例如：你好 世界"
        } else {
            "粘贴 URL 编码后的内容，例如：%E4%BD%A0%E5%A5%BD"
        },
        hint = if (encodeMode) {
            "按 URL 组件编码（同 encodeURIComponent）：中文、空格、& = ? / 等都会写成 %XX，空格是 %20。"
        } else {
            "把 %XX 按 UTF-8 还原成字符，并把 + 当作空格（从网址参数里复制出来的值基本都是这样）。"
        },
        resultLabel = if (encodeMode) "编码结果" else "解码结果",
        convert = { text ->
            if (encodeMode) {
                UrlCodec.encode(text)
            } else {
                UrlCodec.decode(text) ?: "不是合法的 URL 编码：% 后面要跟两位十六进制"
            }
        },
        onCopyText = onCopyText,
        extra = {
            Card(modifier = Modifier.fillMaxWidth()) {
                ModeRow(
                    title = "编码（文字 → URL）",
                    selected = encodeMode,
                    onClick = { encodeMode = true },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                ModeRow(
                    title = "解码（URL → 文字）",
                    selected = !encodeMode,
                    onClick = { encodeMode = false },
                )
            }
        },
    )
}