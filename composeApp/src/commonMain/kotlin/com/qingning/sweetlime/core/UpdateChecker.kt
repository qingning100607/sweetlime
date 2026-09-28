package com.qingning.sweetlime.core

/**
 * 「检查更新」。
 *
 * 这里不会去骚扰任何服务器：只有当 [FEED_URL] 填了地址时才会发一次 GET。
 * 约定很简单 —— 那个地址返回的纯文本里&#8203;包含一个版本号（例如 `2.5.1` 或
 * `{"version":"2.5.1"}`）就行，其余内容会被忽略。
 * 留空则直接告诉用户「当前已是最新」，并引导到交流群。
 */
object UpdateChecker {

    /** 把这里改成你自己的版本号地址（例如 GitHub raw 上的一个 txt）。留空 = 未配置。 */
    const val FEED_URL: String = ""

    sealed interface Result {
        /** 没配地址，只报告当前版本。 */
        data class NotConfigured(val current: String) : Result

        /** 网络失败 / 返回内容里没有版本号。 */
        data class Failed(val current: String) : Result

        /** 已经是最新。 */
        data class UpToDate(val current: String) : Result

        /** 有新版本。 */
        data class Newer(val latest: String, val current: String) : Result
    }

    suspend fun check(): Result {
        val current = APP_VERSION
        val url = FEED_URL
        if (url.isBlank()) return Result.NotConfigured(current)
        val text = httpGetText(url)?.trim().orEmpty()
        val latest = VERSION_PATTERN.find(text)?.value ?: return Result.Failed(current)
        return if (compare(latest, current) > 0) Result.Newer(latest, current) else Result.UpToDate(current)
    }

    private val VERSION_PATTERN = Regex("[0-9]+(\\.[0-9]+)+")

    /** 逐段比大小；段数不一样时缺的地位零（2.5 与 2.5.0 视为相同）。 */
    private fun compare(a: String, b: String): Int {
        val x = a.split('.').map { it.toIntOrNull() ?: 0 }
        val y = b.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(x.size, y.size)) {
            val r = (x.getOrNull(i) ?: 0).compareTo(y.getOrNull(i) ?: 0)
            if (r != 0) return r
        }
        return 0
    }
}
