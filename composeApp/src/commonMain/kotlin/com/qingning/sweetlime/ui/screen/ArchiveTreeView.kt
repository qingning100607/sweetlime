package com.qingning.sweetlime.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qingning.sweetlime.core.ArchiveEntry
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 目录树里的一个节点。 */
private class ZipNode(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    var size: Int,
) {
    val children = ArrayList<ZipNode>()
}

/** 摊平之后、真正拿去渲染的一行。 */
private class ZipRow(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Int,
    val depth: Int,
)

/**
 * 压缩包目录：把解压出来的条目铺成可展开的树。
 *
 * 只负责「看 + 选」—— 选中某个文件之后怎么编辑，交给调用方（代码编辑器）接管。
 */
@Composable
internal fun ArchiveDirectoryPane(
    archiveName: String,
    entries: List<ArchiveEntry>,
    expanded: Set<String>,
    onToggleDirectory: (String) -> Unit,
    onOpenFile: (ArchiveEntry) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val byPath = remember(entries) { entries.associateBy { it.path } }
    val rows = remember(entries, expanded) { flattenTree(buildTree(entries), expanded) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = 48.dp),
    ) {
        item(key = "archive_head") {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$archiveName · ${entries.count { !it.isDirectory }} 个文件",
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    text = "退出目录",
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                SmallTitle(text = "目录 · 点文件夹展开，点文件打开")
            }
        }
        items(items = rows, key = { it.path }) { row ->
            BasicComponent(
                title = buildString {
                    if (row.isDirectory) {
                        append(if (row.path in expanded) "▾ " else "▸ ")
                        append(row.name)
                        append("/")
                    } else {
                        append(row.name)
                    }
                },
                summary = if (row.isDirectory) "目录" else formatSize(row.size),
                onClick = {
                    if (row.isDirectory) {
                        onToggleDirectory(row.path)
                    } else {
                        byPath[row.path]?.let(onOpenFile)
                    }
                },
                modifier = Modifier.padding(start = (row.depth * 16).dp),
            )
        }
    }
}

/** 把扁平的路径列表拼成一棵树。同名的目录节点只建一次。 */
private fun buildTree(entries: List<ArchiveEntry>): ZipNode {
    val root = ZipNode("", "", true, 0)
    val index = HashMap<String, ZipNode>()
    index[""] = root
    for (entry in entries) {
        val trimmed = entry.path.trimEnd('/')
        if (trimmed.isEmpty()) continue
        val parts = trimmed.split('/')
        var parentPath = ""
        for ((i, part) in parts.withIndex()) {
            val last = i == parts.lastIndex
            val path = if (parentPath.isEmpty()) part else "$parentPath/$part"
            // 中间层一定是目录，只有最后一段才可能是文件。
            val isDirectory = if (last) entry.isDirectory else true
            val existing = index[path]
            if (existing == null) {
                val node = ZipNode(
                    name = part,
                    path = path,
                    isDirectory = isDirectory,
                    size = if (last && !isDirectory) entry.bytes.size else 0,
                )
                index[path] = node
                index[parentPath]?.children?.add(node)
            } else if (last && !isDirectory) {
                existing.size = entry.bytes.size
            }
            parentPath = path
        }
    }
    sortTree(root)
    return root
}

/** 目录排前面，其余按名字（忽略大小写）排。 */
private fun sortTree(node: ZipNode) {
    node.children.sortWith(
        compareByDescending<ZipNode> { it.isDirectory }.thenBy { it.name.lowercase() },
    )
    node.children.forEach { sortTree(it) }
}

/** 按当前展开状态把树摊成一行行；折叠的目录下面的内容直接跳过。 */
private fun flattenTree(
    node: ZipNode,
    expanded: Set<String>,
    depth: Int = 0,
    out: MutableList<ZipRow> = ArrayList(),
): List<ZipRow> {
    for (child in node.children) {
        out.add(ZipRow(child.name, child.path, child.isDirectory, child.size, depth))
        if (child.isDirectory && child.path in expanded) {
            flattenTree(child, expanded, depth + 1, out)
        }
    }
    return out
}

private fun formatSize(bytes: Int): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${(bytes / 1024.0 * 10).toInt() / 10.0} KB"
    else -> "${(bytes / (1024.0 * 1024) * 10).toInt() / 10.0} MB"
}