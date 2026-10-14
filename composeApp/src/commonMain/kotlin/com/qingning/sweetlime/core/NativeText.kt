package com.qingning.sweetlime.core

/**
 * 原生实现的入口（花体映射 / MD5 / Base64 / URL 编解码）。
 *
 * 为什么是 expect/actual 而不是直接在 commonMain 调 JNI：common 源集看不到 androidMain，
 * 而这份工程将来要是加别的目标平台，那边给个「都返回 null」的 actual 就能自动回落到
 * Kotlin 实现，调用点一行都不用改。
 *
 * 约定：**返回 null 一律表示「原生这边没结果」**，调用方接着走 Kotlin 版 ——
 * 所以这里永远不能把「原生算出来的合法结果」表达成 null（那几个函数因此返回字节/字符串）。
 */
internal expect object NativeText {
    /** 原生库是否可用（非 Android 平台恒为 false）。 */
    val available: Boolean

    fun md5Hex(bytes: ByteArray): String?

    fun base64Encode(bytes: ByteArray): String?

    /** 解码成原始字节；遇到非法字符返回 null。 */
    fun base64Decode(text: String): ByteArray?

    fun urlEncode(bytes: ByteArray): String?

    /** 解码成原始字节；`%` 后面不是两位十六进制就返回 null。 */
    fun urlDecodeBytes(text: String): ByteArray?

    /** 花体映射；这个 id 不归原生管时返回 null。 */
    fun mapStyle(id: String, text: String): String?
}
