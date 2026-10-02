package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.tools.Md5
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * MD5 计算：输入一段文字，给出 32 位小写（主结果）、32 位大写与 16 位形式。
 *
 * 三种形式其实是一个结果的不同写法，所以主结果交给转换页的通用骨架，
 * 另外两种（大写 / 16 位）放在输入框下面的卡里，点一下直接复制。
 */
@Composable
internal fun Md5ToolScreen(onCopyText: (String, String) -> Unit) {
    ConverterToolScreen(
        placeholder = tr("输入要计算 MD5 的文字"),
        hint = tr("按 UTF-8 计算，空格与换行也算在内。MD5 只是校验摘要，不能当加密或密码散列用。"),
        resultLabel = tr("MD5（32 位小写）"),
        convert = { Md5.hash(it) },
        onCopyText = onCopyText,
        extraForInput = { input ->
            val upper = Md5.hash(input).uppercase()
            val short = Md5.hash16(input)
            Card(modifier = Modifier.fillMaxWidth()) {
                HashRow(
                    title = tr("32 位大写"),
                    value = upper,
                    onCopy = { onCopyText(upper, tr("32 位大写")) },
                )
                HashRow(
                    title = tr("16 位（取中间）"),
                    value = short,
                    onCopy = { onCopyText(short, tr("16 位（取中间）")) },
                )
            }
        },
    )
}

/** 一行「名称 + 结果 + 复制图标」，点整行也是复制。 */
@Composable
private fun HashRow(title: String, value: String, onCopy: () -> Unit) {
    BasicComponent(
        title = title,
        summary = value,
        onClick = onCopy,
        endActions = {
            Icon(
                imageVector = MiuixIcons.Copy,
                contentDescription = tr("复制"),
                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        },
    )
}