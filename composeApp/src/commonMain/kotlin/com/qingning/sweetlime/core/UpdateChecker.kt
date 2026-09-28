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

    /** 更新源（主）：本仓库最新的一条 Release。 */
    const val FEED_URL: String = "https://api.github.com/repos/qingning100607/sweetlime/releases/latest"

    /**
     * 更新源（备用）：仓库里的 version.json，走 raw CDN。
     *
     * GitHub API 对匿名请求有频率限制（同一出口 IP 每小时 60 次，共享网络/VPN 很容易撞到），
     * raw 没有这个限制，所以主源取不到东西时自动降级到这里。
     */
    const val FALLBACK_FEED_URL: String =
        "https://raw.githubusercontent.com/qingning100607/sweetlime/master/version.json"

    /** 发布页（拿不到具体某版的地址时退到这里，永远有地方可去）。 */
    const val RELEASES_PAGE_URL: String =
        "https://github.com/qingning100607/sweetlime/releases"

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
        // 主源：Releases API（能顺带给出发布页地址）。
        var parsed = parseLatest(httpGetText(FEED_URL).orEmpty())
        // 备用源：仓库里的 version.json。API 撞匿名限流时靠它兜底。
        if (parsed == null) parsed = parseLatest(httpGetText(FALLBACK_FEED_URL).orEmpty())
        val (latest, releaseUrl) = parsed ?: return Result.Failed(current)
        return if (compareVersions(latest, current) > 0) {
            Result.Newer(latest, current, releaseUrl)
        } else {
            Result.UpToDate(current)
        }
    }

    /**
     * 主页顶部那条「发现新版本」提示：该不该显示、显示什么。
     *
     * - 只有 [Result.Newer]（确实查到更高的版本）才显示；
     * - 用户点过「×」忽略过的那个版本（[dismissedVersion] 和它相同）就不再打扰，
     *   等出了更高的一版会重新提示；
     * - 还没查（[result] 为 null）、已是最新、检查失败、没配更新源 —— 一律不显示。
     */
    fun homeBanner(result: Result?, dismissedVersion: String): Result.Newer? =
        (result as? Result.Newer)?.takeIf {
            it.latest.isNotBlank() && it.latest != dismissedVersion
        }

    /**
     * 从响应文本里取出版本号（能取到时连发布页地址一起给出）。取不到返回 null。
     */
    internal fun parseLatest(text: String): Pair<String, String?>? {
        if (text.isBlank()) return null
        TAG_PATTERN.find(text)?.let { return it.groupValues[1] to releaseLink(text) }
        KEYED_VERSION_PATTERN.find(text)?.let { return it.groupValues[1] to releaseLink(text) }
        // GitHub 撞限流 / 报错时返回的是 {"message":"API rate limit exceeded for 1.2.3.4"...}，
        // 里面的 IP 长得跟版本号一模一样，所以这种响应一律不认，别去扫数字。
        if (ERROR_BODY_PATTERN.containsMatchIn(text)) return null
        // 最后才退化到「正文里第一个像版本号的东西」，方便临时换成别的纯文本源。
        VERSION_PATTERN.find(text)?.let { return it.value to releaseLink(text) }
        return null
    }

    /** 发布页地址：先认 Releases API 的 `html_url`，再认 version.json 里的 `url`。 */
    private fun releaseLink(text: String): String? {
        val match = RELEASE_URL_PATTERN.find(text) ?: FALLBACK_URL_PATTERN.find(text)
        return match?.groupValues?.getOrNull(1)
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

    /** 备用源 version.json 里的 `"url": "https://github.com/.../releases/tag/v2.5.0"`。 */
    private val FALLBACK_URL_PATTERN = Regex("\"url\"\\s*:\\s*\"(https://github\\.com/[^\"]+)\"")

    /** 备用源 version.json 里的 `"version": "2.5.0"`。 */
    private val KEYED_VERSION_PATTERN = Regex("\"version\"\\s*:\\s*\"v?([0-9]+(?:\\.[0-9]+)*)\"")

    /** GitHub 的错误响应体（限流、404 等），这里面的 IP 不能当成版本号。 */
    private val ERROR_BODY_PATTERN = Regex("\"(message|documentation_url)\"\\s*:")

    /** 兜底：正文里第一个像版本号的东西。 */
    private val VERSION_PATTERN = Regex("[0-9]+(\\.[0-9]+)+")
}