package com.qingning.sweetlime.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.RenderEffect

/** 把文本写入系统剪贴板。 */
expect fun copyToClipboard(text: String)

/** 读系统剪贴板；里面没有文本时返回 null。 */
expect fun readClipboard(): String?

/** 唤起系统的「分享」面板，把文本发给别的 App（微信 / QQ / 短信…）。 */
expect fun shareText(text: String)

/** 用系统浏览器 / 对应 App 打开一个链接。 */
expect fun openUrl(url: String)

/**
 * 「柔光玻璃」渲染效果：全分辨率原生高斯模糊 + 一点提亮 / 加饱和，
 * 就是 HyperOS 柔光玻璃（HyperLight 那种）奶乎乎的感觉。
 * [radiusPx] 为像素半径；[saturation] 1f = 不变；[brightness] 1f = 不变。
 */
expect fun softBlurEffect(radiusPx: Float, saturation: Float, brightness: Float): RenderEffect

/** 拉一段纯文本（用于「检查更新」），失败返回 null。 */
expect suspend fun httpGetText(url: String): String?

/** 极简的键值持久化抽象，避免为一件小事引入整套 DataStore。 */
interface KeyValueStore {
    fun getString(key: String, defaultValue: String): String
    fun putString(key: String, value: String)
    fun getStringSet(key: String): Set<String>
    fun putStringSet(key: String, values: Set<String>)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int)
}

expect fun createKeyValueStore(): KeyValueStore

/** 从文件选择器里选中的一个文件：显示名 + 原始字节。 */
class PickedFile(
    val name: String,
    val bytes: ByteArray,
    /** 系统给这个文件的句柄（Android 上是 content:// 的 uri），写回原文件时要用。 */
    val handle: String? = null,
    /** 这个文件当文本读时用的编码；存回原文件时沿用，免得把 GBK 的文件改成 UTF-8。 */
    val charset: String? = null,
) {
    /** 当文本看；编码会自己猜（严格 UTF-8 → GB18030 → 宽松 UTF-8）。 */
    val text: String get() = decodeTextBytes(bytes)
}

/** 压缩包里的一个条目。 */
class ArchiveEntry(
    /** 包内相对路径，例如 `src/Main.kt`。 */
    val path: String,
    val isDirectory: Boolean,
    val bytes: ByteArray,
    /**
     * 这份 zip 里文件名用的编码（"UTF-8" / "GB18030"）。
     *
     * 留着是为了写回时用同一套编码，免得原包里 GBK 的中文名被改成 UTF-8 后
     * 在 Windows 上一堆花码。
     */
    val charset: String? = null,
    /**
     * 这个条目的**内容**用的编码（和上面的文件名编码是两回事）。
     */
    val contentCharset: String? = null,
)

/** 文件选择器愿意读进来的上限（MB），再大就拒绝，免得把内存吃光。 */
const val MAX_PICK_MEGABYTES = 32

/** 一个压缩包解压后允许占用的总内存上限（MB）；超过就当成解压炸弹不干。 */
const val MAX_UNZIP_MEGABYTES = 96

/** 一个压缩包里最多接受多少个条目。 */
const val MAX_UNZIP_ENTRIES = 4096

/**
 * 把一段字节当文本解码。先按严格 UTF-8 试，不行再试 GB18030（GBK 的超集），
 * 都不行才用宽松 UTF-8 收场 —— 目的就是让 GBK 的老文件 / 中文名 zip 不乱码。
 */
expect fun decodeTextBytes(bytes: ByteArray): String

/**
 * 唤起系统文件选择器挑一个文件（任意类型）。
 *
 * 返回一个「调用即开始选」的函数；用户取消、文件过大或读取失败时回调 null。
 * 之所以写成 @Composable expect：Android 侧要用 ActivityResultLauncher，
 * 而它只能由 Compose 持有。
 */
@Composable
expect fun rememberFilePicker(onResult: (PickedFile?) -> Unit): () -> Unit

/**
 * 把 zip 压缩包的字节解开成条目列表；下面任一情况返回 null：
 *
 * - 不是合法 zip / 解压出错
 * - 条目数超过 [MAX_UNZIP_ENTRIES]
 * - 解压后总量超过 [MAX_UNZIP_MEGABYTES]（防 zip 炸弹：小小的包解出几十 G）
 *
 * 目录条目也会保留（[ArchiveEntry.isDirectory]），方便直接铺成目录树。
 */
expect fun unzipEntries(bytes: ByteArray): List<ArchiveEntry>?

/**
 * 把字节写回 [handle] 指向的那个文件（覆盖原内容）；成功返回 true。
 *
 * 覆盖前会先在内存里留一份原内容，写失败会尽力还原；但这不是事务，
 * 掉电之类的硬中断仍可能留下半截文件。
 */
expect suspend fun writeBackPickedFile(handle: String, bytes: ByteArray): Boolean

/**
 * 把一组条目重新打包成 zip 并直接流式写回 [handle]（不先在内存里拼出整个包）。
 *
 * 文件名编码沿用条目里记的 [ArchiveEntry.charset]；失败返回 false。
 */
expect suspend fun writeZipBackPickedFile(handle: String, entries: List<ArchiveEntry>): Boolean

/** 猜这段字节最可能是哪种文本编码（UTF-8 / UTF-8-BOM / UTF-16LE / UTF-16BE / GB18030）。 */
expect fun detectTextCharset(bytes: ByteArray): String

/**
 * 按指定编码把文本编回字节。
 *
 * 保存回原文件时必须用它，而不是默认 UTF-8 —— 否则一个 GBK 的老文件
 * 打开时显示正常（读取会猜编码），一存就整篇变成 UTF-8 了。
 */
expect fun encodeTextToBytes(text: String, charsetName: String): ByteArray
