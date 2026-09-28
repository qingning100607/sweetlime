package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 更新检查的解析与版本比较（不联网，纯文本进、结果出）。
 */
class UpdateCheckerTest {

    /** GitHub Releases API 的响应片段，字段顺序和真实响应一致。 */
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
        // 换成随便一个纯文本地址（未接入 GitHub 时）也要能用。
        val parsed = UpdateChecker.parseLatest("latest version: 2.5.1\n")
        assertEquals("2.5.1", parsed?.first)
        assertNull(parsed?.second)
    }

    @Test
    fun ignoresTextWithoutVersion() {
        assertNull(UpdateChecker.parseLatest("404: Not Found"))
        // 日期里有横杠没有点，不算版本号。
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
    fun feedIsConfigured() {
        // 更新源接好了，不再走「未配置」那条分支。
        assertTrue(UpdateChecker.FEED_URL.startsWith("https://api.github.com/repos/"))
    }
}