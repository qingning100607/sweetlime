package com.qingning.sweetlime.ui.screen

import com.qingning.sweetlime.core.downloadToDownloads
import com.qingning.sweetlime.core.httpGetDocument
import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.readClipboard
import com.qingning.sweetlime.core.tools.ResourceSniffer
import com.qingning.sweetlime.core.tools.ResourceSniffer.Kind
import com.qingning.sweetlime.ui.effect.TopBarInsetSpacer
import com.qingning.sweetlime.ui.effect.hyperScrollHaptic
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import com.qingning.sweetlime.ui.components.rememberRemoteImage
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Paste
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical

/** 页面里最多跟进多少个 CSS：再多也没意义，还容易把一整站资源拉一遍。 */
private const val MAX_CSS_FILES = 8

/** 列表缩略图解码到 160px、预览图解码到 1600px（够看细节又不会太吃内存）。 */
private const val THUMB_PIXELS = 160
private const val PREVIEW_PIXELS = 1600

/**
 * 网页资源嗅探：给一个页面地址，列出它引用的图片 / 音视频 / 样式表 / 脚本等。
 *
 * 做法和浏览器扩展那类嗅探器一致的两步：
 * 1. 拉页面 HTML，静态解析出所有资源地址；
 * 2. 再跟进页面里的 CSS，把 CSS 里 `url()` 指的图片、字体也扒出来。
 */
@Composable
internal fun SnifferToolScreen(
    onCopyText: (String, String) -> Unit,
    onMessage: (String) -> Unit,
) {
    var url by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var stage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var found by remember { mutableStateOf<List<ResourceSniffer.Resource>>(emptyList()) }
    var scannedCss by remember { mutableStateOf(0) }
    var preview by remember { mutableStateOf<ResourceSniffer.Resource?>(null) }
    // 正在下载的地址（同一时刻同一个地址不允许重复点）
    var downloading by remember { mutableStateOf<Set<String>>(emptySet()) }
    // 嗅探出来的页面地址：下载时当 Referer 用，能过掉不少图床的防盗链
    var pageUrl by remember { mutableStateOf<String?>(null) }
    // 是否已经嗅探过（用于区分「还没开始」和「扫了但没东西」两种空态）
    var done by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun sniff() {
        val target = url.trim()
        if (target.isBlank() || busy) return
        busy = true
        error = null
        found = emptyList()
        scannedCss = 0
        done = false
        stage = tr("正在请求页面…")
        scope.launch {
            // 用户可能只敲了 example.com，补个 https 免得直接失败。
            val normalized = if (target.startsWith("http://") || target.startsWith("https://")) {
                target
            } else {
                "https://$target"
            }
            val page = httpGetDocument(normalized)
            if (page == null) {
                error = tr("打不开这个地址：网络失败、超时，或者返回的不是 2xx。检查一下链接。")
                busy = false
                stage = ""
                return@launch
            }
            pageUrl = page.finalUrl
            val collected = ResourceSniffer.sniffHtml(page.text, page.finalUrl).toMutableList()

            // 第二层：跟进样式表。浏览器嗅探器之所以能列出背景图、字体，就是靠这一步。
            val cssUrls = collected
                .filter { it.kind == Kind.STYLE && it.from == null }
                .map { it.url }
                .distinct()
                .take(MAX_CSS_FILES)
            for ((index, cssUrl) in cssUrls.withIndex()) {
                stage = trf("正在解析样式表…（{}/{}）", index + 1, cssUrls.size)
                val css = httpGetDocument(cssUrl) ?: continue
                scannedCss = index + 1
                for (item in ResourceSniffer.sniffCss(css.text, cssUrl, from = cssUrl)) {
                    if (collected.none { it.url == item.url }) collected.add(item)
                }
            }
            found = collected
            done = true
            stage = ""
            busy = false
        }
    }

    // 下载：图片和视频走的是同一条路（MediaStore 写进「下载/SweetLime」）。
    fun download(resource: ResourceSniffer.Resource) {
        val target = resource.url
        if (downloading.contains(target)) return
        downloading = downloading + target
        scope.launch {
            val saved = downloadToDownloads(
                url = target,
                fileName = ResourceSniffer.suggestFileName(target),
                referer = pageUrl,
            )
            downloading = downloading - target
            onMessage(
                if (saved != null) {
                    trf("已保存到 {}", saved)
                } else {
                    tr("下载失败，检查网络或换个地址试试。")
                },
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .overScrollVertical()
            .hyperScrollHaptic(),
        contentPadding = PaddingValues(bottom = 24.dp, start = 12.dp, end = 12.dp),
    ) {
        item {
            Column {
                TopBarInsetSpacer()
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = url,
                    onValueChange = { url = it },
                    label = tr("输入网页地址，例如 https://www.example.com"),
                    useLabelAsPlaceholder = true,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = tr("只做静态解析：JS 动态插入的资源、XHR 拉的数据扫不到。"),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { readClipboard()?.let { url = it } },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Paste,
                            contentDescription = tr("粘贴"),
                            tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    text = if (found.isEmpty()) tr("开始嗅探") else tr("重新嗅探"),
                    onClick = { sniff() },
                    enabled = !busy && url.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )

                if (busy) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stage.ifEmpty { tr("正在处理…") },
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                error?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }

        if (found.isNotEmpty() && !busy) {
            item(key = "sniff_summary") {
                Column {
                    SmallTitle(
                        text = trf(
                            "共 {} 个资源 · 点条目复制链接",
                            found.size,
                        ),
                    )
                    TextButton(
                        text = tr("复制全部链接"),
                        onClick = {
                            onCopyText(found.joinToString("\n") { it.url }, tr("全部链接"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            // 按类型分组：图片一组、音视频一组…，和拖进浏览器调试面板看到的分组是一个意思。
            for (kind in Kind.values()) {
                val items = found.filter { it.kind == kind }
                if (items.isEmpty()) continue
                item(key = "sniff_head_${kind.name}") {
                    SmallTitle(text = trf("{} · {} 个", kindLabel(kind), items.size))
                }
                item(key = "sniff_body_${kind.name}") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        items.forEach { resource ->
                            ResourceRow(
                                resource = resource,
                                previewable = kind == Kind.IMAGE,
                                downloading = downloading.contains(resource.url),
                                onCopy = { onCopyText(resource.url, ResourceSniffer.suggestFileName(resource.url)) },
                                onPreview = { preview = resource },
                                onDownload = { download(resource) },
                            )
                        }
                    }
                }
            }
            item(key = "sniff_tail") { Spacer(modifier = Modifier.height(48.dp)) }
        } else if (!busy && found.isEmpty() && stage.isEmpty() && error == null) {
            item(key = "sniff_empty") {
                Text(
                    text = if (!done) {
                        tr("还没嗅探：填个网页地址，点上面的按钮。")
                    } else {
                        tr("没解析到资源：页面可能全靠 JS 渲染，或者这个地址不是网页。")
                    },
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }

    // 点缩略图 → 大图预览。用系统的 Dialog：盖在整页之上，点空白就关。
    preview?.let { target ->
        Dialog(onDismissRequest = { preview = null }) {
            val big = rememberRemoteImage(target.url, PREVIEW_PIXELS)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (big != null) {
                        Image(
                            bitmap = big,
                            contentDescription = tr("预览图"),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = tr("正在加载图片…"),
                                style = MiuixTheme.textStyles.footnote1,
                                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = target.url,
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TextButton(
                            text = tr("下载"),
                            onClick = { download(target) },
                            enabled = !downloading.contains(target.url),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                        TextButton(
                            text = tr("复制链接"),
                            onClick = {
                                onCopyText(target.url, ResourceSniffer.suggestFileName(target.url))
                                preview = null
                            },
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(
                            text = tr("关闭"),
                            onClick = { preview = null },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

/** 类型名。 */
private fun kindLabel(kind: Kind): String = when (kind) {
    Kind.IMAGE -> tr("图片")
    Kind.MEDIA -> tr("音视频")
    Kind.FONT -> tr("字体")
    Kind.STYLE -> tr("样式表")
    Kind.SCRIPT -> tr("脚本")
    Kind.FRAME -> tr("框架页")
    Kind.OTHER -> tr("其它")
}



/**
 * 一行资源。
 *
 * 图片类多一个缩略图、并且「点整行 = 看大图」（复制交给右侧图标）；
 * 其它类型没有预览可说，点整行就直接复制链接。
 */
@Composable
private fun ResourceRow(
    resource: ResourceSniffer.Resource,
    previewable: Boolean,
    downloading: Boolean,
    onCopy: () -> Unit,
    onPreview: () -> Unit,
    onDownload: () -> Unit,
) {
    val thumb = if (previewable) rememberRemoteImage(resource.url, THUMB_PIXELS) else null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (previewable) onPreview() else onCopy() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (previewable) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center,
            ) {
                if (thumb != null) {
                    Image(
                        bitmap = thumb,
                        contentDescription = tr("图片缩略图"),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Text(
                        text = tr("载入中"),
                        style = MiuixTheme.textStyles.footnote1,
                        color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ResourceSniffer.suggestFileName(resource.url),
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurfaceContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = resource.url,
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (downloading) {
            Text(
                text = tr("下载中…"),
                style = MiuixTheme.textStyles.footnote1,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        } else {
            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = MiuixIcons.Download,
                    contentDescription = tr("下载"),
                    tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                )
            }
        }
        IconButton(onClick = onCopy) {
            Icon(
                imageVector = MiuixIcons.Copy,
                contentDescription = tr("复制"),
                tint = MiuixTheme.colorScheme.onSurfaceContainerVariant,
            )
        }
    }
}
