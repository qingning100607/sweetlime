package com.qingning.sweetlime.core

/**
 * 原生实现（Android）：加载 `libsweetlime.so`，把调用转给 JNI。
 *
 * 加载失败（例如将来某个 ABI 没编进去）时 [available] 为 false，所有调用返回 null，
 * 上层自动回落到 Kotlin 实现 —— 功能不受影响，只是慢一点。
 */
internal actual object NativeText {

    private const val LIB = "sweetlime"

    private val loaded: Boolean = try {
        System.loadLibrary(LIB)
        true
    } catch (_: Throwable) {
        // **刻意不写日志**：单测跑在 JVM 上，android.util.Log 是桩，一调就抛，
        // 会把整个 object 的初始化带崩（ExceptionInInitializerError）。
        // 加载失败不影响功能：所有调用返回 null，上层自动回落到 Kotlin 实现。
        false
    }

    actual val available: Boolean get() = loaded

    actual fun md5Hex(bytes: ByteArray): String? = if (loaded) nativeMd5(bytes) else null

    actual fun base64Encode(bytes: ByteArray): String? =
        if (loaded) nativeBase64Encode(bytes) else null

    actual fun base64Decode(text: String): ByteArray? =
        if (loaded) nativeBase64Decode(text) else null

    actual fun urlEncode(bytes: ByteArray): String? =
        if (loaded) nativeUrlEncode(bytes) else null

    actual fun urlDecodeBytes(text: String): ByteArray? =
        if (loaded) nativeUrlDecodeBytes(text) else null

    actual fun mapStyle(id: String, text: String): String? =
        if (loaded) nativeMapStyle(id, text) else null

    // 名字必须和 cpp/native_text.cpp 里的 JNI 符号一一对应（改一个要改两边）。
    private external fun nativeMd5(bytes: ByteArray): String
    private external fun nativeBase64Encode(bytes: ByteArray): String
    private external fun nativeBase64Decode(text: String): ByteArray?
    private external fun nativeUrlEncode(bytes: ByteArray): String
    private external fun nativeUrlDecodeBytes(text: String): ByteArray?
    private external fun nativeMapStyle(id: String, text: String): String?
}
