package com.qingning.sweetlime.core

/**
 * 「检查更新」。
 *
 * 只在 [FEED_URL] 非空时才会发一次 GET，平时不会去碰任何服务器。
 *
 * 更新源是本仓库的 GitHub Releases：发版时在 GitHub 上打一个 tag（形如 `v2.6.0`）
 * 并发布 Release 即可，应用内就能看到。该接口不带授权也能访问（有匿名频率限制，
 * 但「检查更新」这点请求量远远够用）。
 *
 * 解析很宽松，按顺序尝试：
 * 1. `"tag_name": "v2.6.0"` —— GitHub Releases 的 JSON；
 * 2. 正文里第一个形如 `2.6.0` 的版本号 —— 换成任何纯文本地址也能用。
 */
object UpdateChecker {

    /** 更新源：本仓库最新的一条 Release。 */
    const val FEED_URL: String = "https://api.github.com/repos/qingning100607/sweetlime/releases/latest"

    sealed interface Result {
        /** 没配地址，只报告当前版本。 */
        data class NotConfigured(val current: String) : Result

        /** 网络失败 / 返回内容里没有版本号 / 还没有发布过 Release。 */
        data class Failed(val current: String) : Result

        /** 已经是最新。 */
        data class UpToDate(val current: String) : Result

        /**
         * 有新版本。
         *
         * [releaseUrl] 是发布页地址，取不到时为 null（界面会退回到「去交流群」的说法）。
         */
        data class Newer(
            val latest: String,
            val current: String,
            val releaseUrl: String? = null,
        ) : Result
    }

    suspend fun check(): Result {
        val current = APP_VERSION
        val url = FEED_URL
        if (url.isBlank()) return Result.NotConfigured(current)
        val text = httpGetText(url)?.trim().orEmpty()
        val (latest, releaseUrl) = parseLatest(text) ?: return Result.Failed(current)
        return if (compareVersions(latest, current) > 0) {
            Result.Newer(latest, current, releaseUrl)
        } else {
            Result.UpToDate(current)
        }
    }

    /**
     * 从响应文本里取出版本号（能取到时连发布页地址一起给出）。取不到返回 null。
     */
    internal fun parseLatest(text: String): Pair<String, String?>? {
        TAG_PATTERN.find(text)?.let { match ->
            val url = RELEASE_URL_PATTERN.find(text)?.groupValues?.getOrNull(1)
            return match.groupValues[1] to url
        }
        VERSION_PATTERN.find(text)?.let { return it.value to null }
        return null
    }

    /** 逐段比大小；段数不一样时缺的地位零（2.5 与 2.5.0 视为相同）。 */
    internal fun compareVersions(a: String, b: String): Int {
        val x = a.split('.').map { it.toIntOrNull() ?: 0 }
        val y = b.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(x.size, y.size)) {
            val r = (x.getOrNull(i) ?: 0).compareTo(y.getOrNull(i) ?: 0)
            if (r != 0) return r
        }
        return 0
    }

    /** GitHub Releases JSON 里的 `"tag_name": "v2.6.0"`（`v` 可有可无）。 */
    private val TAG_PATTERN = Regex("\"tag_name\"\\s*:\\s*\"v?([0-9]+(?:\\.[0-9]+)*)\"")

    /** 发布页地址，只认 GitHub 自己的域名，避免把响应里别的链接当成下载地址。 */
    private val RELEASE_URL_PATTERN = Regex("\"html_url\"\\s*:\\s*\"(https://github\\.com/[^\"]+)\"")

    /** 兜底：正文里第一个像版本号的东西。 */
    private val VERSION_PATTERN = Regex("[0-9]+(\\.[0-9]+)+")
}