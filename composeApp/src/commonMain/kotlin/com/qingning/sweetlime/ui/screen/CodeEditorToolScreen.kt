package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import top.yukonga.miuix.kmp.utils.overScrollVertical
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingning.sweetlime.core.ArchiveEntry
import com.qingning.sweetlime.core.MAX_PICK_MEGABYTES
import com.qingning.sweetlime.core.MAX_UNZIP_MEGABYTES
import com.qingning.sweetlime.core.PickedFile
import com.qingning.sweetlime.core.rememberFilePicker
import com.qingning.sweetlime.core.unzipEntries
import com.qingning.sweetlime.core.writeBackPickedFile
import com.qingning.sweetlime.core.decodeTextBytes
import com.qingning.sweetlime.core.encodeTextToBytes
import com.qingning.sweetlime.core.writeZipBackPickedFile
import com.qingning.sweetlime.ui.components.CodeHighlighter
import com.qingning.sweetlime.ui.components.CodePalette
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 代码编辑用的等宽样式。行号与正文共用，保证两边行高一致、能对齐。 */
private val CodeTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontSize = 14.sp,
    lineHeight = 20.sp,
)

/** zip 的魔数：PK\003\004 / PK\005\006 / PK\007\008。 */
private fun looksLikeZipHeader(bytes: ByteArray): Boolean {
    if (bytes.size < 4) return false
    return bytes[0] == 0x50.toByte() &&
        bytes[1] == 0x4B.toByte() &&
        (bytes[2] == 3.toByte() || bytes[2] == 5.toByte() || bytes[2] == 7.toByte())
}

/** 扩展名或魔数任一对得上，就当压缩包试一把。 */
private fun looksLikeArchive(file: PickedFile): Boolean =
    file.name.endsWith(".zip", ignoreCase = true) || looksLikeZipHeader(file.bytes)

/** 默认要展开的顶层目录。 */
private fun firstLevelDirs(entries: List<ArchiveEntry>): Set<String> =
    entries.mapNotNull { entry ->
        val path = entry.path.trimEnd('/')
        if (path.contains('/')) path.substringBefore('/') else null
    }.toSet()

/**
 * 代码编辑区：行号 + 等宽字体 + 语法高亮。
 *
 * 抽出来是为了让编辑器本页和「压缩包里打开的文件」共用同一套观感 ——
 * 两处各写一遍的话，改一处忘一处迟早会不一样。
 */
@Composable
internal fun CodeEditorField(
    text: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 高亮配色跟着背景明暗走：深色背景用 One Dark，浅色用 GitHub Light。
    val dark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val palette = if (dark) CodePalette.Dark else CodePalette.Light
    val highlighter = remember(dark) {
        VisualTransformation { input ->
            TransformedText(
                text = CodeHighlighter.highlight(input.text, palette),
                // 上色不改变字符数，偏移量一一对应。
                offsetMapping = OffsetMapping.Identity,
            )
        }
    }

    // 光标位置得自己拿着，否则没法做「补右括号」这种输入期改写。
    var field by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    // 外部改了文本（导入文件 / 清空 / 查找替换）就同步过来。
    LaunchedEffect(text) {
        if (field.text != text) {
            field = TextFieldValue(text, TextRange(text.length))
        }
    }
    val lineCount = field.text.count { it == '\n' } + 1
    // 行号栏宽度跟着位数走，99 行以内保持两字符宽，不会左右跳。
    val gutterWidth = (lineCount.toString().length.coerceAtLeast(2) * 12).dp

    Row(modifier = modifier) {
        Column(
            modifier = Modifier
                .width(gutterWidth)
                .padding(end = 6.dp),
            horizontalAlignment = Alignment.End,
        ) {
            for (line in 1..lineCount) {
                Text(
                    text = "$line",
                    style = CodeTextStyle,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant.copy(alpha = 0.55f),
                )
            }
        }
        BasicTextField(
            value = field,
            onValueChange = { raw ->
                val edited = autoEdit(field, raw)
                field = edited
                onValueChange(edited.text)
            },
            textStyle = CodeTextStyle.copy(color = MiuixTheme.colorScheme.onSurfaceContainer),
            cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            visualTransformation = highlighter,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
        )
    }
}

/**
 * 代码编辑器：等宽字体 + 行号 + 语法高亮。
 *
 * 「导入文件」有两副面孔：普通文本直接铺进编辑区；**压缩包自动解压成目录树**，
 * 点里面的文件就地打开继续编辑（不写回压缩包）。
 */
@Composable
internal fun CodeEditorToolScreen(onCopyText: (String, String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var fileName by rememberSaveable { mutableStateOf<String?>(null) }

    // 下面这几个只有「从压缩包里打开」时才有值。
    var archiveName by rememberSaveable { mutableStateOf<String?>(null) }
    var openedPath by rememberSaveable { mutableStateOf<String?>(null) }
    var browsing by rememberSaveable { mutableStateOf(false) }
    var entries by remember { mutableStateOf<List<ArchiveEntry>>(emptyList()) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var message by remember { mutableStateOf<String?>(null) }
    var findOpen by rememberSaveable { mutableStateOf(false) }
    var findText by rememberSaveable { mutableStateOf("") }
    var replaceText by rememberSaveable { mutableStateOf("") }
    var nextIndex by remember { mutableStateOf(0) }
    var archiveHandle by remember { mutableStateOf<String?>(null) }
    var fileHandle by remember { mutableStateOf<String?>(null) }
    var appendNext by remember { mutableStateOf(false) }
    // 当前这份文本原本是什么编码；存回去时按原样写，不硬改 UTF-8。
    var openedCharset by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val pickFile = rememberFilePicker { picked ->
        val append = appendNext
        appendNext = false
        when {
            picked == null -> {
                message = trf("没选到文件（或者它超过 {} MB）。", MAX_PICK_MEGABYTES)
            }

            looksLikeArchive(picked) -> {
                val list = unzipEntries(picked.bytes)
                if (list.isNullOrEmpty()) {
                    message = trf("「{}」不是能读的 zip，或者解压后超过 {} MB。", picked.name, MAX_UNZIP_MEGABYTES)
                } else {
                    message = null
                    archiveName = picked.name
                    archiveHandle = picked.handle
                    fileHandle = null
                    openedCharset = null
                    entries = list
                    expanded = firstLevelDirs(list)
                    text = ""
                    fileName = null
                    openedPath = null
                    browsing = true
                }
            }

            else -> {
                message = null
                archiveName = null
                entries = emptyList()
                expanded = emptySet()
                openedPath = null
                browsing = false
                text = if (append) text + picked.text else picked.text
                fileName = picked.name
                fileHandle = picked.handle
                openedCharset = picked.charset
            }
        }
    }

    val currentArchive = archiveName
    val currentOpenedPath = openedPath
    if (browsing && currentArchive != null && entries.isNotEmpty()) {
        ArchiveDirectoryPane(
            archiveName = currentArchive,
            entries = entries,
            expanded = expanded,
            onToggleDirectory = { path ->
                expanded = if (path in expanded) expanded - path else expanded + path
            },
            onOpenFile = { entry ->
                text = decodeTextBytes(entry.bytes)
                openedPath = entry.path
                openedCharset = entry.contentCharset
                fileName = entry.path.substringAfterLast('/')
                browsing = false
            },
            onExit = { browsing = false },
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    val lineCount = text.count { it == '\n' } + 1
    val matchCount = remember(text, findText) { countOccurrences(text, findText) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()).overScrollVertical()
            .padding(horizontal = 12.dp),
    ) {
        // 顶栏是浮层：这点高度必须写在「滚动内容」里，内容才能滑到顶栏下面被实时糊。
        TopBarInsetSpacer()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildString {
                currentOpenedPath?.let { append("$it · ") }
                    ?: fileName?.let { append("$it · ") }
                append(trf("{} 行 · {} 字符", lineCount, text.length))
            },
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        message?.let { text0 ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text0,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        if (currentArchive != null && currentOpenedPath != null) {
            TextButton(
                text = trf("返回目录 · {}", currentArchive),
                onClick = { browsing = true },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                text = tr("保存到压缩包"),
                onClick = {
                    val path = currentOpenedPath
                    val handle = archiveHandle
                    if (handle == null) {
                        message = tr("这个压缩包的来源拿不到写入句柄，存不回去。")
                    } else {
                        scope.launch {
                            val updated = entries.map { entry ->
                                if (entry.path == path) {
                                    ArchiveEntry(
                                        path = entry.path,
                                        isDirectory = entry.isDirectory,
                                        bytes = encodeTextToBytes(text, openedCharset ?: "UTF-8"),
                                        charset = entry.charset,
                                        contentCharset = entry.contentCharset,
                                    )
                                } else {
                                    entry
                                }
                            }
                            if (writeZipBackPickedFile(handle, updated)) {
                                entries = updated
                                message = trf("已保存回 {}", currentArchive)
                            } else {
                                message = tr("这个位置不让写（可能是只读目录），原内容已尽量还原，换个包再试。")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        if (fileHandle != null && currentOpenedPath == null) {
            TextButton(
                text = tr("保存回原文件"),
                onClick = {
                    val handle = fileHandle
                    if (handle == null) {
                        message = tr("这个文件的来源拿不到写入句柄，存不回去。")
                    } else {
                        scope.launch {
                            if (writeBackPickedFile(handle, encodeTextToBytes(text, openedCharset ?: "UTF-8"))) {
                                message = tr("已保存回 ") + (fileName ?: tr("原文件"))
                            } else {
                                message = tr("这个位置不让写（可能是只读目录），换个文件再试。")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
            Spacer(modifier = Modifier.height(6.dp))
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            CodeEditorField(
                text = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Spacer(modifier = Modifier.height(6.dp))
        FindReplaceSection(
            open = findOpen,
            onToggleOpen = { findOpen = !findOpen },
            findText = findText,
            onFindTextChange = { value ->
                findText = value
                nextIndex = 0
            },
            replaceText = replaceText,
            onReplaceTextChange = { value -> replaceText = value },
            matchCount = matchCount,
            onReplaceNext = {
                if (findText.isEmpty()) {
                    message = tr("先写上要查找的内容。")
                } else {
                    val at = indexOfFrom(text, findText, nextIndex)
                    if (at < 0) {
                        message = trf("没找到「{}」。", findText)
                    } else {
                        text = text.substring(0, at) + replaceText +
                            text.substring(at + findText.length)
                        nextIndex = at + replaceText.length
                        message = tr("已替换 1 处。")
                    }
                }
            },
            onReplaceAll = {
                if (matchCount == 0) {
                    message = trf("没找到「{}」。", findText)
                } else {
                    text = text.replace(findText, replaceText)
                    nextIndex = 0
                    message = trf("已替换 {} 处。", matchCount)
                }
            },
        )
        Text(
            text = tr("高亮覆盖常见语言的注释 / 字符串 / 数字 / 关键词，只影响观感、不改内容。") +
                tr("导入 zip 会自动解压铺成目录，点里面的文件就地打开；普通文件改完能存回原文件，包内文件改完能存回原压缩包。") +
                tr("输入时会自动补右括号、换行保留缩进。"),
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                text = tr("导入文件"),
                onClick = { pickFile() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(
                text = tr("追加导入"),
                onClick = {
                    appendNext = true
                    pickFile()
                },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(
                text = tr("清空"),
                onClick = {
                    text = ""
                    fileName = null
                    openedPath = null
                },
                enabled = text.isNotEmpty(),
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(12.dp))
            TextButton(
                text = tr("复制全部"),
                onClick = { if (text.isNotEmpty()) onCopyText(text, tr("代码")) },
                enabled = text.isNotEmpty(),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
            )
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}