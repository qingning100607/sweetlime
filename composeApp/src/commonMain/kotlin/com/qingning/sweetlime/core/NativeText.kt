package com.qingning.sweetlime.core

internal expect object NativeText {

    val available: Boolean

    fun md5Hex(bytes: ByteArray): String?

    fun base64Encode(bytes: ByteArray): String?

    fun base64Decode(text: String): ByteArray?

    fun urlEncode(bytes: ByteArray): String?

    fun urlDecodeBytes(text: String): ByteArray?

    fun mapStyle(id: String, text: String): String?
}
