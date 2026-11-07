package com.qingning.sweetlime.core

object UpdateChecker {

    const val FEED_URL: String = "https://api.github.com/repos/qingning100607/sweetlime/releases/latest"

    const val FALLBACK_FEED_URL: String =
        "https://raw.githubusercontent.com/qingning100607/sweetlime/master/version.json"

    const val RELEASES_PAGE_URL: String =
        "https://github.com/qingning100607/sweetlime/releases"

    const val REPO_URL: String = "https://github.com/qingning100607/sweetlime"

    data class Feed(
        val version: String,

        val pageUrl: String? = null,

        val apkUrl: String? = null,
        val notes: String? = null,
    )

    sealed interface Result {

        data class NotConfigured(val current: String) : Result

        data class Failed(val current: String) : Result

        data class UpToDate(val current: String) : Result

        data class Newer(
            val latest: String,
            val current: String,
            val releaseUrl: String? = null,

            val apkUrl: String? = null,
            val notes: String? = null,
        ) : Result
    }

    suspend fun check(): Result {
        val current = APP_VERSION

        var feed = parseFeed(httpGetText(FEED_URL).orEmpty())

        if (feed == null) feed = parseFeed(httpGetText(FALLBACK_FEED_URL).orEmpty())
        val latest = feed?.version ?: return Result.Failed(current)
        return if (compareVersions(latest, current) > 0) {
            Result.Newer(latest, current, feed.pageUrl, feed.apkUrl, feed.notes)
        } else {
            Result.UpToDate(current)
        }
    }

    fun homeBanner(result: Result?, dismissedVersion: String): Result.Newer? =
        (result as? Result.Newer)?.takeIf {
            it.latest.isNotBlank() && it.latest != dismissedVersion
        }

    internal fun parseLatest(text: String): Pair<String, String?>? =
        parseFeed(text)?.let { it.version to it.pageUrl }

    internal fun parseFeed(text: String): Feed? {
        if (text.isBlank()) return null
        val apk = apkLink(text)
        val notes = bodyText(text)
        TAG_PATTERN.find(text)?.let { return Feed(it.groupValues[1], releaseLink(text), apk, notes) }
        KEYED_VERSION_PATTERN.find(text)?.let {
            return Feed(it.groupValues[1], releaseLink(text), apk, notes)
        }

        if (ERROR_BODY_PATTERN.containsMatchIn(text)) return null

        VERSION_PATTERN.find(text)?.let { return Feed(it.value, releaseLink(text), apk, notes) }
        return null
    }

    private fun apkLink(text: String): String? =
        (APK_URL_PATTERN.find(text) ?: KEYED_APK_PATTERN.find(text))?.groupValues?.getOrNull(1)

    private fun bodyText(text: String): String? {
        val raw = (BODY_PATTERN.find(text) ?: KEYED_NOTES_PATTERN.find(text))
            ?.groupValues?.getOrNull(1) ?: return null
        val out = StringBuilder(raw.length)
        var i = 0
        while (i < raw.length) {
            val ch = raw[i]
            if (ch == '\\' && i + 1 < raw.length) {
                when (val next = raw[i + 1]) {
                    'n' -> out.append('\n')
                    'r' -> out.append('\r')
                    't' -> out.append('\t')
                    'b' -> out.append('\b')
                    '"' -> out.append('"')
                    '\\' -> out.append('\\')
                    '/' -> out.append('/')
                    'u' -> {
                        val hex = raw.substring(i + 2, minOf(i + 6, raw.length))
                        val code = hex.toIntOrNull(16)
                        if (code != null) {
                            out.append(code.toChar())
                            i += 4
                        }
                    }
                    else -> out.append(next)
                }
                i += 2
            } else {
                out.append(ch)
                i++
            }
        }
        return out.toString().trim().ifBlank { null }
    }

    private fun releaseLink(text: String): String? {
        val match = RELEASE_URL_PATTERN.find(text) ?: FALLBACK_URL_PATTERN.find(text)
        return match?.groupValues?.getOrNull(1)
    }

    internal fun compareVersions(a: String, b: String): Int {
        val x = a.split('.').map { it.toIntOrNull() ?: 0 }
        val y = b.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(x.size, y.size)) {
            val r = (x.getOrNull(i) ?: 0).compareTo(y.getOrNull(i) ?: 0)
            if (r != 0) return r
        }
        return 0
    }

    private val TAG_PATTERN = Regex("\"tag_name\"\\s*:\\s*\"v?([0-9]+(?:\\.[0-9]+)*)\"")

    private val RELEASE_URL_PATTERN = Regex("\"html_url\"\\s*:\\s*\"(https://github\\.com/[^\"]+)\"")

    private val FALLBACK_URL_PATTERN = Regex("\"url\"\\s*:\\s*\"(https://github\\.com/[^\"]+)\"")

    private val APK_URL_PATTERN = Regex("\"browser_download_url\"\\s*:\\s*\"(https://[^\"]+\\.apk)\"")

    private val BODY_PATTERN = Regex("\"body\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")
    private val KEYED_NOTES_PATTERN = Regex("\"notes\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")

    private val KEYED_APK_PATTERN = Regex("\"apk\"\\s*:\\s*\"(https://[^\"]+)\"")

    private val KEYED_VERSION_PATTERN = Regex("\"version\"\\s*:\\s*\"v?([0-9]+(?:\\.[0-9]+)*)\"")

    private val ERROR_BODY_PATTERN = Regex("\"(message|documentation_url)\"\\s*:")

    private val VERSION_PATTERN = Regex("[0-9]+(\\.[0-9]+)+")
}