package com.qingning.sweetlime.core

import java.util.Locale
import com.qingning.sweetlime.core.i18n.AppLanguage
import com.qingning.sweetlime.core.i18n.tr
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import com.qingning.sweetlime.AppContext
import com.qingning.sweetlime.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import java.io.File
import java.io.OutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContract
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.atan2
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
@Composable
actual fun rememberLimeLogo(): Painter = painterResource(R.drawable.lime)
@Composable
actual fun rememberLanguageIcon(): Painter = painterResource(R.drawable.ic_language)

actual fun copyToClipboard(text: String) {
    val manager = AppContext.get().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    manager.setPrimaryClip(ClipData.newPlainText("SweetLime", text))
}

actual fun readClipboard(): String? {
    val manager = AppContext.get().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = manager.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(AppContext.get())?.toString()?.takeIf { it.isNotBlank() }
}

actual fun shareText(text: String) {
    val context = AppContext.get()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    val chooser = Intent.createChooser(intent, tr("分享到")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(chooser) }
}

actual fun openUrl(url: String) {
    val context = AppContext.get()
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

actual fun softBlurEffect(radiusPx: Float, saturation: Float, brightness: Float): RenderEffect {
    return try {
        val matrix = android.graphics.ColorMatrix().apply { setSaturation(saturation) }
            .apply {
                val bright = android.graphics.ColorMatrix().apply {
                    setScale(brightness, brightness, brightness, 1f)
                }
                postConcat(bright)
            }
        val blurred = android.graphics.RenderEffect.createBlurEffect(
            radiusPx,
            radiusPx,
            android.graphics.Shader.TileMode.CLAMP,
        )
        android.graphics.RenderEffect.createColorFilterEffect(
            android.graphics.ColorMatrixColorFilter(matrix),
            blurred,
        ).asComposeRenderEffect()
    } catch (_: Throwable) {

        androidx.compose.ui.graphics.BlurEffect(
            radiusPx,
            radiusPx,
            androidx.compose.ui.graphics.TileMode.Clamp,
        )
    }
}

actual suspend fun httpGetText(url: String): String? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8000
            readTimeout = 8000
            requestMethod = "GET"
            setRequestProperty("Accept", "text/plain, application/json")
            setRequestProperty("User-Agent", "SweetLime/${APP_VERSION}")
        }
        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

actual fun systemUiLanguage(): AppLanguage {

    val locale = Locale.getDefault()
    return when {
        locale.language == "zh" -> {
            val traditional = locale.script == "Hant" ||
                locale.country.uppercase() in setOf("TW", "HK", "MO")
            if (traditional) AppLanguage.TW else AppLanguage.ZH
        }
        locale.language == "en" -> AppLanguage.EN
        else -> AppLanguage.ZH
    }
}

private class AndroidKeyValueStore(
    private val prefs: SharedPreferences,
) : KeyValueStore {
    override fun getString(key: String, defaultValue: String): String =
        prefs.getString(key, defaultValue) ?: defaultValue

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun getStringSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet()) ?: emptySet()

    override fun putStringSet(key: String, values: Set<String>) {
        prefs.edit().putStringSet(key, values.toSet()).apply()
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        prefs.getBoolean(key, defaultValue)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    override fun getInt(key: String, defaultValue: Int): Int =
        prefs.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
}

actual fun createKeyValueStore(): KeyValueStore {
    val prefs = AppContext.get().getSharedPreferences("sweetlime", Context.MODE_PRIVATE)
    return AndroidKeyValueStore(prefs)
}

@Composable
actual fun rememberFilePicker(onResult: (PickedFile?) -> Unit): () -> Unit {
    val context = AppContext.get()
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = OpenDocumentForWrite(),
    ) { uri: Uri? ->
        if (uri == null) {
            onResult(null)
        } else {
            scope.launch {
                onResult(withContext(Dispatchers.IO) { readPickedFile(context, uri) })
            }
        }
    }
    return remember(launcher) { { launcher.launch(arrayOf("*/*")) } }
}

private fun readPickedFile(context: Context, uri: Uri): PickedFile? = try {
    val name = queryDisplayName(context, uri) ?: tr("已导入文件")
    val limit = MAX_PICK_MEGABYTES * 1024L * 1024L
    val declaredSize = querySize(context, uri)
    if (declaredSize != null && declaredSize > limit) {
        null
    } else {
        val stream = context.contentResolver.openInputStream(uri)
        if (stream == null) {
            null
        } else {
            val bytes = stream.use { it.readBytes() }
            if (bytes.size > limit) {
                null
            } else {
                PickedFile(name, bytes, uri.toString(), detectTextCharset(bytes))
            }
        }
    }
} catch (t: Throwable) {
    null
}

private fun querySize(context: Context, uri: Uri): Long? = runCatching {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (index >= 0) cursor.getLong(index) else null
    }
}.getOrNull()

private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return@use null
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0) cursor.getString(index) else null
    }
}.getOrNull()

private fun strictUtf8(bytes: ByteArray): String? = runCatching {
    Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()
}.getOrNull()

private fun strictDecode(bytes: ByteArray, charset: Charset): String? = runCatching {
    charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()
}.getOrNull()

actual fun decodeTextBytes(bytes: ByteArray): String {
    if (bytes.isEmpty()) return ""

    if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) {
        return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
    }
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
    }
    if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
    }
    strictUtf8(bytes)?.let { return it }

    strictDecode(bytes, Charset.forName("GB18030"))?.let { return it }

    return bytes.decodeToString()
}

actual fun detectTextCharset(bytes: ByteArray): String {
    if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() &&
        bytes[2] == 0xBF.toByte()
    ) {
        return "UTF-8-BOM"
    }
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
        return "UTF-16LE"
    }
    if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
        return "UTF-16BE"
    }
    if (strictUtf8(bytes) != null) return "UTF-8"
    if (strictDecode(bytes, Charset.forName("GB18030")) != null) return "GB18030"
    return "UTF-8"
}

actual fun encodeTextToBytes(text: String, charsetName: String): ByteArray {
    return runCatching {
        when (charsetName) {
            "UTF-8-BOM" -> byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
                text.toByteArray(Charsets.UTF_8)

            "UTF-16LE" -> byteArrayOf(0xFF.toByte(), 0xFE.toByte()) +
                text.toByteArray(Charsets.UTF_16LE)

            "UTF-16BE" -> byteArrayOf(0xFE.toByte(), 0xFF.toByte()) +
                text.toByteArray(Charsets.UTF_16BE)

            "GB18030" -> text.toByteArray(Charset.forName("GB18030"))
            else -> text.toByteArray(Charsets.UTF_8)
        }
    }.getOrElse { text.encodeToByteArray() }
}

actual fun unzipEntries(bytes: ByteArray): List<ArchiveEntry>? {

    val utf8 = readZipEntries(bytes, "UTF-8")
    if (utf8 != null && utf8.none { it.path.contains('\uFFFD') }) return utf8
    val legacy = readZipEntries(bytes, "GB18030")
    return legacy ?: utf8
}

private fun readZipEntries(bytes: ByteArray, charsetName: String): List<ArchiveEntry>? = runCatching {
    val charset = Charset.forName(charsetName)
    val limit = MAX_UNZIP_MEGABYTES * 1024L * 1024L
    val result = ArrayList<ArchiveEntry>()
    var total = 0L
    ZipInputStream(ByteArrayInputStream(bytes), charset).use { zip ->
        val buffer = ByteArray(64 * 1024)
        while (true) {
            val entry = zip.nextEntry ?: break
            if (result.size >= MAX_UNZIP_ENTRIES) return null
            if (entry.isDirectory) {
                result.add(ArchiveEntry(entry.name, true, ByteArray(0), charsetName))
            } else {
                val out = ByteArrayOutputStream()
                while (true) {
                    val read = zip.read(buffer)
                    if (read <= 0) break
                    total += read
                    if (total > limit) return null
                    out.write(buffer, 0, read)
                }
                val content = out.toByteArray()
                result.add(
                    ArchiveEntry(
                        path = entry.name,
                        isDirectory = false,
                        bytes = content,
                        charset = charsetName,
                        contentCharset = detectTextCharset(content),
                    ),
                )
            }
            zip.closeEntry()
        }
    }
    result
}.getOrNull()

private class OpenDocumentForWrite : ActivityResultContract<Array<String>, Uri?>() {

    override fun createIntent(context: Context, input: Array<String>): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "*/*"
        if (input.isNotEmpty()) intent.putExtra(Intent.EXTRA_MIME_TYPES, input)
        intent.addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
        )
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK) return null
        return intent?.data
    }
}

private inline fun writeWithRollback(uri: Uri, write: (OutputStream) -> Unit): Boolean {
    val resolver = AppContext.get().contentResolver
    val backup = runCatching {
        resolver.openInputStream(uri)?.use { it.readBytes() }
    }.getOrNull()

    val ok = runCatching {
        val stream = resolver.openOutputStream(uri, "rwt") ?: return@runCatching false
        stream.use { output ->
            write(output)
            output.flush()
        }
        true
    }.getOrDefault(false)

    if (ok) return true
    if (backup != null) {
        runCatching {
            resolver.openOutputStream(uri, "rwt")?.use { it.write(backup) }
        }
    }
    return false
}

actual suspend fun writeBackPickedFile(handle: String, bytes: ByteArray): Boolean =
    withContext(Dispatchers.IO) {
        writeWithRollback(Uri.parse(handle)) { output -> output.write(bytes) }
    }

actual suspend fun writeZipBackPickedFile(
    handle: String,
    entries: List<ArchiveEntry>,
): Boolean = withContext(Dispatchers.IO) {

    val charsetName = entries.firstOrNull { !it.charset.isNullOrEmpty() }?.charset ?: "UTF-8"
    writeWithRollback(Uri.parse(handle)) { output ->
        ZipOutputStream(output, Charset.forName(charsetName)).use { zip ->
            for (entry in entries) {
                zip.putNextEntry(ZipEntry(entry.path))
                if (!entry.isDirectory) zip.write(entry.bytes)
                zip.closeEntry()
            }
        }
    }
}

private const val TILT_FILTER = 0.15f

@Composable
actual fun rememberTilt(): Tilt? {
    val context = AppContext.get()
    var tilt by remember { mutableStateOf<Tilt?>(null) }
    DisposableEffect(context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        var listener: SensorEventListener? = null
        if (manager != null && sensor != null) {
            val l = object : SensorEventListener {
                private var gx = 0f
                private var gy = 0f
                private var gz = 0f

                override fun onSensorChanged(event: SensorEvent) {
                    val v = event.values
                    gx += TILT_FILTER * (v[0] - gx)
                    gy += TILT_FILTER * (v[1] - gy)
                    gz += TILT_FILTER * (v[2] - gz)

                    tilt = Tilt(
                        x = Math.toDegrees(atan2(gx.toDouble(), gz.toDouble())).toFloat(),
                        y = Math.toDegrees(atan2(gy.toDouble(), gz.toDouble())).toFloat(),
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            listener = l

            manager.registerListener(l, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {

            val l = listener
            if (l != null) manager?.unregisterListener(l)
        }
    }
    return tilt
}

private const val MAX_DOCUMENT_BYTES = 4 * 1024 * 1024

private const val BROWSER_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.0.0 Mobile Safari/537.36"

private fun charsetFromContentType(contentType: String?): String? {
    val header = contentType ?: return null
    val value = Regex("charset\\s*=\\s*[\"']?([A-Za-z0-9_\\-]+)", RegexOption.IGNORE_CASE)
        .find(header)?.groupValues?.get(1) ?: return null
    return value.takeIf { runCatching { Charset.forName(it) }.isSuccess }
}

private fun readLimited(input: InputStream, max: Int): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(16 * 1024)
    var total = 0
    while (true) {
        val n = input.read(buffer)
        if (n < 0) break
        if (total + n >= max) {
            out.write(buffer, 0, max - total)
            break
        }
        out.write(buffer, 0, n)
        total += n
    }
    return out.toByteArray()
}

actual suspend fun httpGetDocument(url: String): HttpResponse? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 15000
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", BROWSER_UA)
            setRequestProperty(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,text/css,*/*;q=0.8",
            )
            setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
        }
        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val bytes = connection.inputStream.use { readLimited(it, MAX_DOCUMENT_BYTES) }

            val finalUrl = connection.url?.toString() ?: url
            if (bytes.isEmpty()) return@runCatching HttpResponse("", finalUrl)
            val byHeader = charsetFromContentType(connection.contentType)
                ?.let { name -> runCatching { bytes.toString(Charset.forName(name)) }.getOrNull() }
            HttpResponse(byHeader ?: decodeTextBytes(bytes), finalUrl)
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

private const val MAX_IMAGE_BYTES = 12 * 1024 * 1024

private fun decodeSampled(bytes: ByteArray, maxPixels: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)

    while (longest / sample > maxPixels * 2) sample *= 2

    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return null
    return bitmap.asImageBitmap()
}

actual suspend fun loadImageBitmap(url: String, maxPixels: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 15000
                setRequestProperty("User-Agent", BROWSER_UA)
                setRequestProperty("Accept", "image/*,*/*;q=0.8")
            }
            try {
                if (connection.responseCode !in 200..299) return@runCatching null
                val bytes = connection.inputStream.use { readLimited(it, MAX_IMAGE_BYTES) }
                if (bytes.isEmpty()) return@runCatching null
                decodeSampled(bytes, maxPixels)
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

private fun sanitizeFileName(raw: String): String {
    val cleaned = raw
        .substringBefore('?')
        .substringBefore('#')
        .replace('\\', '_')
        .replace('/', '_')
        .trim()
    val safe = cleaned.ifEmpty { "sweetlime_file" }

    if (safe.length <= 96) return safe
    val dot = safe.lastIndexOf('.')
    val ext = if (dot in 1..10) safe.substring(dot) else ""
    return safe.take(90 - ext.length) + ext
}

private fun downloadFilePath(name: String): String = File(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
    "SweetLime/$name",
).absolutePath

actual suspend fun downloadToDownloads(
    url: String,
    fileName: String,
    referer: String?,
    onProgress: (Float) -> Unit,
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val context = AppContext.get()
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 30000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", BROWSER_UA)

            if (!referer.isNullOrEmpty()) setRequestProperty("Referer", referer)
        }
        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val total = connection.contentLengthLong
            val mime = connection.contentType?.substringBefore(';')?.trim().orEmpty()
            val resolver = context.contentResolver
            val name = sanitizeFileName(fileName)
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(
                    MediaStore.MediaColumns.MIME_TYPE,
                    mime.ifEmpty { "application/octet-stream" },
                )
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/SweetLime",
                )

                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return@runCatching null
            try {
                connection.inputStream.use { input ->
                    val output = resolver.openOutputStream(uri)
                        ?: throw IllegalStateException("openOutputStream failed")
                    output.use { out ->
                        val buffer = ByteArray(64 * 1024)
                        var done = 0L
                        var lastPercent = -1
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            out.write(buffer, 0, read)
                            done += read
                            if (total > 0) {
                                val percent = (done * 100 / total).toInt()

                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    onProgress(percent / 100f)
                                }
                            }
                        }
                    }
                }
            } catch (t: Throwable) {

                runCatching { resolver.delete(uri, null, null) }
                throw t
            }
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)

            val actualName = runCatching {
                resolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)
                    ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
            }.getOrNull() ?: name
            downloadFilePath(actualName)
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}
