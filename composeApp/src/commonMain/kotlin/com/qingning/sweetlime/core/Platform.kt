package com.qingning.sweetlime.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.painter.Painter

expect fun copyToClipboard(text: String)

expect fun readClipboard(): String?

expect fun shareText(text: String)

expect fun openUrl(url: String)

expect fun softBlurEffect(radiusPx: Float, saturation: Float, brightness: Float): RenderEffect

expect fun systemUiLanguage(): com.qingning.sweetlime.core.i18n.AppLanguage

expect suspend fun httpGetText(url: String): String?

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

class PickedFile(
    val name: String,
    val bytes: ByteArray,

    val handle: String? = null,

    val charset: String? = null,
) {

    val text: String get() = decodeTextBytes(bytes)
}

class ArchiveEntry(

    val path: String,
    val isDirectory: Boolean,
    val bytes: ByteArray,

    val charset: String? = null,

    val contentCharset: String? = null,
)

const val MAX_PICK_MEGABYTES = 32

const val MAX_UNZIP_MEGABYTES = 96

const val MAX_UNZIP_ENTRIES = 4096

expect fun decodeTextBytes(bytes: ByteArray): String

@Composable
expect fun rememberFilePicker(onResult: (PickedFile?) -> Unit): () -> Unit

expect fun unzipEntries(bytes: ByteArray): List<ArchiveEntry>?

expect suspend fun writeBackPickedFile(handle: String, bytes: ByteArray): Boolean

expect suspend fun writeZipBackPickedFile(handle: String, entries: List<ArchiveEntry>): Boolean

@Composable
expect fun rememberLimeLogo(): Painter

@Composable
expect fun rememberLanguageIcon(): Painter

expect fun detectTextCharset(bytes: ByteArray): String

data class HttpResponse(val text: String, val finalUrl: String)

expect suspend fun httpGetDocument(url: String): HttpResponse?

expect suspend fun loadImageBitmap(url: String, maxPixels: Int = 1200): ImageBitmap?

expect suspend fun downloadToDownloads(
    url: String,
    fileName: String,
    referer: String? = null,
    onProgress: (Float) -> Unit = {},
): String?

expect fun encodeTextToBytes(text: String, charsetName: String): ByteArray

data class Tilt(val x: Float, val y: Float)

@Composable
expect fun rememberTilt(): Tilt?
