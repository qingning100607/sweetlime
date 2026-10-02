package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import com.qingning.sweetlime.AppContext
import com.qingning.sweetlime.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    // 单独放一个新 task：从 Application context 起 Activity 必须带这个 flag。
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

/**
 * 「柔光玻璃」：全分辨率原生高斯模糊 + 颜色矩阵（提亮 + 加饱和）。
 *
 * 1. 模糊用 [android.graphics.RenderEffect.createBlurEffect]，不做任何降采样，
 *    所以文字不会被糊成一块一块的（miuix 自带 blur() 会降采样，半径大就有像素感）；
 * 2. 颜色矩阵把底下的内容稍微提亮、加一点饱和，压成一片奶白色，
 *    这就是 HyperOS 「柔光玻璃」那种质感。
 */
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
        // 个别机型不支持链式 RenderEffect，退化成纯模糊，不影响可用。
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

actual suspend fun downloadApkToPrivateDir(
    url: String,
    fileName: String,
    onProgress: (Float) -> Unit,
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val dir = File(AppContext.get().filesDir, "update").apply { mkdirs() }
        val part = File(dir, "$fileName.part")
        val dest = File(dir, fileName)
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 20000
            // GitHub 的下载地址会 302 到 objects.githubusercontent.com，得跟着走。
            instanceFollowRedirects = true
            requestMethod = "GET"
            setRequestProperty("User-Agent", "SweetLime/${APP_VERSION}")
        }
        try {
            if (connection.responseCode !in 200..299) return@runCatching null
            val total = connection.contentLengthLong
            connection.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var done = 0L
                    // 只在整数百分比变化时回调：几 MB 的包能少掉上千次无谓的状态更新。
                    var lastPercent = -2
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        output.write(buffer, 0, read)
                        done += read
                        val percent = if (total > 0) ((done * 100) / total).toInt() else -1
                        if (percent != lastPercent) {
                            lastPercent = percent
                            onProgress(
                                if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else -1f,
                            )
                        }
                    }
                    output.flush()
                }
            }
            // 只有一个完整的包才会被改名成正式文件名 —— 半截的留在 .part，不会拿去安装。
            if (dest.exists()) dest.delete()
            if (!part.renameTo(dest)) return@runCatching null
            onProgress(1f)
            dest.absolutePath
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

actual fun installApkFile(path: String): Boolean {
    val context = AppContext.get()
    val file = File(path)
    if (!file.exists()) return false
    // Android 8 起装别的应用要先有「安装未知应用」权限。
    // 第一次点大概率就是卡在这里 —— 以前直接 startActivity，系统默默拦掉，
    // 用户看到的就是"点了没反应"。现在缺权限就主动把授权页打开。
    if (!context.packageManager.canRequestPackageInstalls()) {
        return openInstallPermissionSettings()
    }
    val launched = runCatching {
        // Android 7 起不能用 file:// 把文件交给别的应用，必须换成 content:// 并临时授权。
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }.isSuccess
    // 有权限但没安装器能接（个别系统把 intent 拦了）：也弹授权页，别让用户点了没反应。
    return launched || openInstallPermissionSettings()
}

/** 打开本应用的「安装未知应用」授权页；打不开返回 false。 */
private fun openInstallPermissionSettings(): Boolean = runCatching {
    val context = AppContext.get()
    val intent = Intent(
        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
        Uri.parse("package:${context.packageName}"),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}.isSuccess

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

/** 从 content:// 里把一个文件读成字节；过大或任何环节出错都返回 null。 */
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

/** 声明的大小（拿不到就返回 null，那就只能读完再判断）。 */
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

/** 严格 UTF-8；有非法字节就返回 null（GB18030 的文件基本过不了这一关）。 */
private fun strictUtf8(bytes: ByteArray): String? = runCatching {
    Charsets.UTF_8.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()
}.getOrNull()

/** 指定编码严格解码；字节不合法就返回 null。 */
private fun strictDecode(bytes: ByteArray, charset: Charset): String? = runCatching {
    charset.newDecoder()
        .onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT)
        .decode(ByteBuffer.wrap(bytes))
        .toString()
}.getOrNull()

actual fun decodeTextBytes(bytes: ByteArray): String {
    if (bytes.isEmpty()) return ""
    // BOM 优先，它比任何猜测都准。
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
    // 不是 UTF-8，中文环境里绝大多数就是 GBK 系。
    strictDecode(bytes, Charset.forName("GB18030"))?.let { return it }
    // 兜底：宽松 UTF-8，坏字节变成 U+FFFD，至少不崩。
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
    // 先按 UTF-8 读；名字里出现替换符说明原包用的是另一套编码，换个编码重来一遍。
    val utf8 = readZipEntries(bytes, "UTF-8")
    if (utf8 != null && utf8.none { it.path.contains('\uFFFD') }) return utf8
    val legacy = readZipEntries(bytes, "GB18030")
    return legacy ?: utf8
}

/** 用指定编码把 zip 读成条目；条目数 / 解压总量超限或出错返回 null。 */
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

/**
 * 挑一个文件，并且**同时申请写权限** —— 这样后面才能把内容保存回原文件。
 *
 * 系统自带的 OpenDocument 契约不带写权限，所以这里自己声明一份。
 */
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

/**
 * 覆盖写一个 content:// 文件，写坏了就尽力把原内容还原。
 *
 * openOutputStream(uri, "rwt") 会先把文件截断，之后任何一步出错（磁盘满、进程被杀、
 * 掉电）都会留下半截文件。所以这里先留一份原字节，失败就立刻写回去。
 */
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
    // 用原包的编码打包，中文名才不会在别处变花。
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

/** 低通滤波系数：越大越跟手、越小越稳。0.15 是「气泡不抖、手一动又能跟上」的常见取值。 */
private const val TILT_FILTER = 0.15f

/**
 * 水平仪：订阅加速度计，按「一阶低通 + atan2」算出左右 / 前后倾角。
 *
 * 为什么要滤波：原始加速度噪声很大（手轻轻一抖就跳好几度），气泡会一直蹦，
 * 所以每次只把读数的 15% 混进当前值里。
 */
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
                    // 屏幕朝上水平放置时 gx≈0、gy≈0、gz≈9.8：
                    // atan2(gx, gz) 是左右倾角，atan2(gy, gz) 是前后倾角。
                    tilt = Tilt(
                        x = Math.toDegrees(atan2(gx.toDouble(), gz.toDouble())).toFloat(),
                        y = Math.toDegrees(atan2(gy.toDouble(), gz.toDouble())).toFloat(),
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            listener = l
            // SENSOR_DELAY_GAME ≈ 20ms 一帧，气泡跟得上手。
            manager.registerListener(l, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            // 离开页面立刻注销：传感器一直开着是实打实的耗电。
            val l = listener
            if (l != null) manager?.unregisterListener(l)
        }
    }
    return tilt
}

/** 嗅探一次最多读 4MB 正文：够解析几万个标签，又不至于把内存吃爆。 */
private const val MAX_DOCUMENT_BYTES = 4 * 1024 * 1024

/** 装成普通浏览器：不少站点看 UA 决定给不给你完整页面。 */
private const val BROWSER_UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) " +
        "Chrome/124.0.0.0 Mobile Safari/537.36"

/** 从 Content-Type 里抠出 charset，没有就返回 null。 */
private fun charsetFromContentType(contentType: String?): String? {
    val header = contentType ?: return null
    val value = Regex("charset\\s*=\\s*[\"']?([A-Za-z0-9_\\-]+)", RegexOption.IGNORE_CASE)
        .find(header)?.groupValues?.get(1) ?: return null
    return value.takeIf { runCatching { Charset.forName(it) }.isSuccess }
}

/** 读满 [max] 字节就停（页面里如果挂了超大文件，不至于整段读进来）。 */
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
            // 重定向之后的地址：解析相对链接要用它，不然 /img/a.png 会拼到旧主机上。
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

/** 单张图片最多下 12MB（有些站点的「图」其实是几百 KB 的 GIF，够用了）。 */
private const val MAX_IMAGE_BYTES = 12 * 1024 * 1024

/** 按长边上限算采样率：列表里的小缩略图没必要把整张原图读进内存。 */
private fun decodeSampled(bytes: ByteArray, maxPixels: Int): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sample = 1
    val longest = maxOf(bounds.outWidth, bounds.outHeight)
    // 采样率是 2 的幂；留一倍余量（比如只要 160px，就解成 ≤320px 再缩，画质更稳）
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
