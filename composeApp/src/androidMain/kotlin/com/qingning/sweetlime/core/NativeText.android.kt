package com.qingning.sweetlime.core

internal actual object NativeText {

    private const val LIB = "sweetlime"

    private val loaded: Boolean = try {
        System.loadLibrary(LIB)
        true
    } catch (_: Throwable) {

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

    private external fun nativeMd5(bytes: ByteArray): String
    private external fun nativeBase64Encode(bytes: ByteArray): String
    private external fun nativeBase64Decode(text: String): ByteArray?
    private external fun nativeUrlEncode(bytes: ByteArray): String
    private external fun nativeUrlDecodeBytes(text: String): ByteArray?
    private external fun nativeMapStyle(id: String, text: String): String?
}
