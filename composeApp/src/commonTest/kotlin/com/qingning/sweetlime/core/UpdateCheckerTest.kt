package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateCheckerTest {

    private val releaseJson = """
        {
          "url": "https://api.github.com/repos/qingning100607/sweetlime/releases/1",
          "html_url": "https://github.com/qingning100607/sweetlime/releases/tag/v2.6.0",
          "id": 1234567,
          "tag_name": "v2.6.0",
          "name": "2.6.0",
          "created_at": "2026-10-01T12:00:00Z",
          "body": "修了几个问题"
        }
    """.trimIndent()

    @Test
    fun readsGithubRelease() {
        val parsed = UpdateChecker.parseLatest(releaseJson)
        assertEquals("2.6.0", parsed?.first)
        assertEquals(
            "https://github.com/qingning100607/sweetlime/releases/tag/v2.6.0",
            parsed?.second,
        )
    }

    @Test
    fun readsReleaseWithoutVPrefix() {
        val parsed = UpdateChecker.parseLatest("""{"tag_name":"2.6.0"}""")
        assertEquals("2.6.0", parsed?.first)
    }

    @Test
    fun fallsBackToPlainTextFeed() {

        val parsed = UpdateChecker.parseLatest("latest version: 2.5.1\n")
        assertEquals("2.5.1", parsed?.first)
        assertNull(parsed?.second)
    }

    @Test
    fun ignoresTextWithoutVersion() {
        assertNull(UpdateChecker.parseLatest("404: Not Found"))

        assertNull(UpdateChecker.parseLatest("created 2026-10-01"))
    }

    @Test
    fun comparesSegments() {
        assertEquals(0, UpdateChecker.compareVersions("2.5", "2.5.0"))
        assertTrue(UpdateChecker.compareVersions("2.6", "2.5.10") > 0)
        assertTrue(UpdateChecker.compareVersions("2.10", "2.9") > 0)
        assertTrue(UpdateChecker.compareVersions("2.5.0", "2.5.1") < 0)
    }

    @Test
    fun readsFallbackFile() {

        val parsed = UpdateChecker.parseLatest(
            """{"version":"2.5.0","url":"https://github.com/qingning100607/sweetlime/releases/tag/v2.5.0"}""",
        )
        assertEquals("2.5.0", parsed?.first)
        assertEquals(
            "https://github.com/qingning100607/sweetlime/releases/tag/v2.5.0",
            parsed?.second,
        )
    }

    @Test
    fun ignoresRateLimitBody() {

        assertNull(
            UpdateChecker.parseLatest(
                """{"message":"API rate limit exceeded for 203.10.98.186.","documentation_url":"https://docs.github.com/rest"}""",
            ),
        )
    }

    @Test
    fun feedIsConfigured() {

        assertTrue(UpdateChecker.FEED_URL.startsWith("https://api.github.com/repos/"))
        assertTrue(UpdateChecker.FALLBACK_FEED_URL.startsWith("https://raw.githubusercontent.com/"))
    }

    @Test
    fun homeBannerShowsOnlyWhenNewer() {
        val newer = UpdateChecker.Result.Newer("2.6.0", "2.5.3", "https://github.com/x/y/releases/tag/v2.6.0")
        assertEquals(newer, UpdateChecker.homeBanner(newer, ""))

        assertNull(UpdateChecker.homeBanner(null, ""))
        assertNull(UpdateChecker.homeBanner(UpdateChecker.Result.UpToDate("2.5.3"), ""))
        assertNull(UpdateChecker.homeBanner(UpdateChecker.Result.Failed("2.5.3"), ""))
        assertNull(UpdateChecker.homeBanner(UpdateChecker.Result.NotConfigured("2.5.3"), ""))
    }

    @Test
    fun homeBannerRespectsDismiss() {
        val newer = UpdateChecker.Result.Newer("2.6.0", "2.5.3")

        assertNull(UpdateChecker.homeBanner(newer, "2.6.0"))

        assertEquals(newer, UpdateChecker.homeBanner(newer, "2.5.0"))
    }

    @Test
    fun homeBannerNeedsVersionText() {
        assertNull(UpdateChecker.homeBanner(UpdateChecker.Result.Newer("", "2.5.3"), ""))
    }

    private val releaseJsonWithAsset = """
        {
          "html_url": "https://github.com/qingning100607/sweetlime/releases/tag/v2.5.4",
          "tag_name": "v2.5.4",
          "assets": [
            {
              "name": "SweetLime-2.5.4.apk",
              "browser_download_url": "https://github.com/qingning100607/sweetlime/releases/download/v2.5.4/SweetLime-2.5.4.apk"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun readsApkUrlFromReleaseAssets() {
        val feed = UpdateChecker.parseFeed(releaseJsonWithAsset)
        assertEquals("2.5.4", feed?.version)
        assertEquals(
            "https://github.com/qingning100607/sweetlime/releases/download/v2.5.4/SweetLime-2.5.4.apk",
            feed?.apkUrl,
        )
    }

    @Test
    fun readsApkUrlFromVersionJson() {
        val feed = UpdateChecker.parseFeed(
            """{"version":"2.5.4","url":"https://github.com/qingning100607/sweetlime/releases","apk":"https://github.com/qingning100607/sweetlime/releases/download/v2.5.4/SweetLime-2.5.4.apk"}""",
        )
        assertEquals("2.5.4", feed?.version)
        assertTrue(feed?.apkUrl?.endsWith(".apk") == true)
    }

    @Test
    fun noApkUrlWhenSourceHasNone() {

        val feed = UpdateChecker.parseFeed("latest version: 2.5.4")
        assertEquals("2.5.4", feed?.version)
        assertNull(feed?.apkUrl)
    }

    @Test
    fun apkUrlKeepsReleaseUrl() {

        val feed = UpdateChecker.parseFeed(releaseJsonWithAsset)
        assertEquals(
            "https://github.com/qingning100607/sweetlime/releases/tag/v2.5.4",
            feed?.pageUrl,
        )
    }
}