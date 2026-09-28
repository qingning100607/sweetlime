package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 代码编辑器用到的几个纯函数 + 查找替换面板。
 *
 * 单独放一个文件，免得编辑器那页继续膨胀；这些函数都不碰 Compose 状态，好测也好复用。
 */

/** 双引号。写成码点是为了不在代码里到处塞转义符。 */
private const val DOUBLE_QUOTE = 34

/** 单引号。 */
private const val SINGLE_QUOTE = 39

/** 换行。 */
private const val NEWLINE = 10

/** 空格。 */
private const val SPACE = 32

/** 制表符。 */
private const val TAB = 9

/** 开括号 / 引号 → 对应的收尾符号。 */
private val CLOSERS: Map<String, String> = mapOf(
    "(" to ")",
    "[" to "]",
    "{" to "}",
    DOUBLE_QUOTE.toChar().toString() to DOUBLE_QUOTE.toChar().toString(),
    SINGLE_QUOTE.toChar().toString() to SINGLE_QUOTE.toChar().toString(),
)

/** 一共能找到几处（不重叠）。 */
internal fun countOccurrences(source: String, target: String): Int {
    if (target.isEmpty()) return 0
    var count = 0
    var index = source.indexOf(target)
    while (index >= 0) {
        count++
        index = source.indexOf(target, index + target.length)
    }
    return count
}

/** 从 [from] 开始找；找不到就回到开头再找一次，这样「替换下一个」能循环。 */
internal fun indexOfFrom(source: String, target: String, from: Int): Int {
    val start = from.coerceIn(0, source.length)
    val found = source.indexOf(target, start)
    return if (found >= 0) found else source.indexOf(target)
}

/**
 * 输入期的自动加工：补右括号 / 跳过重复的收尾符 / 换行保住缩进。
 *
 * 只在「光标是折叠的、而且刚插入了一个字符」这种最好判断的情况下动手；
 * 粘贴、删除、多字符输入一律原样放行 —— 免得把用户的输入改坏。
 */
internal fun autoEdit(old: TextFieldValue, raw: TextFieldValue): TextFieldValue {
    if (!old.selection.collapsed) return raw
    if (raw.text.length != old.text.length + 1) return raw
    val at = old.selection.start
    if (at < 0 || at > old.text.length) return raw
    val ch = raw.text.getOrNull(at) ?: return raw
    // 确认真的只是「在 at 处插入了一个 ch」，否则不认。
    if (old.text.substring(0, at) + ch + old.text.substring(at) != raw.text) return raw

    val key = ch.toString()
    val next = old.text.getOrNull(at)
    // 1. 打的是收尾符，而下一个字符正好是同一个：跳过去，不重复输入。
    if (next != null && next.toString() == key && key in CLOSERS.values) {
        return TextFieldValue(old.text, TextRange(at + 1))
    }
    val closer = CLOSERS[key]
    if (closer != null) {
        // 2. 引号只在「前面不是字母数字」时才自动配对，
        //    否则英文里的 don't 会被打成不成对的引号。
        val prev = old.text.getOrNull(at - 1)
        val isQuote = key == DOUBLE_QUOTE.toChar().toString() ||
            key == SINGLE_QUOTE.toChar().toString()
        if (!isQuote || prev == null || !prev.isLetterOrDigit()) {
            return TextFieldValue(
                text = old.text.substring(0, at) + ch + closer + old.text.substring(at),
                selection = TextRange(at + 1),
            )
        }
    }
    // 3. 换行：沿用上一行的缩进；上一行以 { ( [ 或 : 结尾就再进一级。
    if (ch.code == NEWLINE) {
        val lineStart = old.text.lastIndexOf(NEWLINE.toChar(), at - 1) + 1
        val prevLine = old.text.substring(lineStart, at)
        val base = prevLine.takeWhile { it.code == SPACE || it.code == TAB }
        val trimmed = prevLine.trimEnd()
        val deeper = trimmed.endsWith("{") || trimmed.endsWith("(") ||
            trimmed.endsWith("[") || trimmed.endsWith(":")
        val indent = if (deeper) base + " " else base
        val tail = old.text.substring(at)
        if (tail.isNotEmpty() && tail[0].toString() in CLOSERS.values) {
            // 下一行就是收尾符：把它单独放到下一行，光标留在中间那一行。
            return TextFieldValue(
                text = old.text.substring(0, at) + NEWLINE.toChar() + indent +
                    NEWLINE.toChar() + base + tail,
                selection = TextRange(at + 1 + indent.length),
            )
        }
        return TextFieldValue(
            text = old.text.substring(0, at) + NEWLINE.toChar() + indent + tail,
            selection = TextRange(at + 1 + indent.length),
        )
    }
    return raw
}

/** 查找替换面板：平时收起，点一下才铺开。 */
@Composable
internal fun FindReplaceSection(
    open: Boolean,
    onToggleOpen: () -> Unit,
    findText: String,
    onFindTextChange: (String) -> Unit,
    replaceText: String,
    onReplaceTextChange: (String) -> Unit,
    matchCount: Int,
    onReplaceNext: () -> Unit,
    onReplaceAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        text = if (open) "收起查找" else "查找替换",
        onClick = onToggleOpen,
        modifier = modifier.fillMaxWidth(),
    )
    if (!open) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            TextField(
                value = findText,
                onValueChange = onFindTextChange,
                label = "查找",
                useLabelAsPlaceholder = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = replaceText,
                onValueChange = onReplaceTextChange,
                label = "替换成",
                useLabelAsPlaceholder = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    findText.isEmpty() -> "先写上要查找的内容。"
                    matchCount == 0 -> "没找到「$findText」。"
                    else -> "找到 $matchCount 处。"
                },
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    text = "替换下一个",
                    onClick = onReplaceNext,
                    enabled = matchCount > 0,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(12.dp))
                TextButton(
                    text = "全部替换",
                    onClick = onReplaceAll,
                    enabled = matchCount > 0,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
