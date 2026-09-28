package com.qingning.sweetlime.ui

import com.qingning.sweetlime.core.UpdateChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 「发现新版本 → 应用内下载 → 唤起安装器」这条链路里的纯文案逻辑。
 *
 * 这些函数不碰网络也不碰文件，纯状态进、文字出，所以能直接单测 ——
 * 界面只是把它们的结果画出来。
 */
class UpdateDownloadStateTest {

    @Test
    fun progressLabelShowsPercent() {
        assertEquals("正在下载 0%", downloadProgressLabel(0f))
        assertEquals("正在下载 42%", downloadProgressLabel(0.42f))
        assertEquals("正在下载 99%", downloadProgressLabel(0.999f))
    }

    @Test
    fun progressLabelHandlesUnknownAndDone() {
        // 服务器没给 Content-Length：只能说「正在下载」，不能编一个百分比出来。
        assertEquals("正在下载…", downloadProgressLabel(-1f))
        assertEquals("下载完成，正在打开安装…", downloadProgressLabel(1f))
    }

    @Test
    fun bannerFollowsDownloadState() {
        val idle = updateBannerText("2.5.4", UpdateDownloadState.Idle)
        assertEquals("发现新版本 2.5.4", idle.title)
        assertTrue(idle.summary.contains("应用内"))

        val downloading = updateBannerText("2.5.4", UpdateDownloadState.Downloading(0.42f))
        assertEquals("正在下载 2.5.4", downloading.title)
        assertEquals("正在下载 42%", downloading.summary)

        val ready = updateBannerText("2.5.4", UpdateDownloadState.Ready("/tmp/x.apk"))
        assertEquals("安装 2.5.4", ready.title)

        // 下载失败要能重试，并且仍然说清是哪个版本。
        val failed = updateBannerText("2.5.4", UpdateDownloadState.Failed)
        assertEquals("发现新版本 2.5.4", failed.title)
        assertTrue(failed.summary.contains("重试"))
    }

    @Test
    fun bannerTellsUserToGrantInstallPermission() {
        // 首次安装最常见的坑：没有「安装未知应用」权限，点安装像没反应。
        // 这种情况横幅必须直说"去授权"，不能还写着"点一下打开系统安装器"。
        val banner = updateBannerText("2.5.4", UpdateDownloadState.NeedsPermission("/tmp/x.apk"))
        assertEquals("安装 2.5.4", banner.title)
        assertTrue(banner.summary.contains("安装未知应用"))
        assertTrue(banner.summary.contains("授权"))
    }

    @Test
    fun rowTextTellsUserToGrantInstallPermission() {
        val row = updateRowText(
            result = UpdateChecker.Result.Newer("2.5.4", "2.5.3"),
            checking = false,
            state = UpdateDownloadState.NeedsPermission("/tmp/x.apk"),
            currentVersion = "2.5.3",
        )
        assertTrue(row.contains("安装未知应用"))
        assertTrue(row.contains("授权"))
    }

    @Test
    fun rowTextPrefersDownloadStateOverCheckResult() {
        // 下载中即使在设置页再点一次「检查更新」，也不该跳回「点击检查是否有新版本」。
        val newer = UpdateChecker.Result.Newer("2.5.4", "2.5.3")
        val downloading = updateRowText(
            result = newer,
            checking = false,
            state = UpdateDownloadState.Downloading(0.3f),
            currentVersion = "2.5.3",
        )
        assertEquals("正在下载 30%", downloading)

        val ready = updateRowText(newer, false, UpdateDownloadState.Ready("/tmp/x.apk"), "2.5.3")
        assertTrue(ready.contains("安装器"))

        val failed = updateRowText(newer, false, UpdateDownloadState.Failed, "2.5.3")
        assertTrue(failed.contains("重试"))
    }

    @Test
    fun rowTextReportsCheckResult() {
        assertEquals(
            "正在检查…",
            updateRowText(null, true, UpdateDownloadState.Idle, "2.5.3"),
        )
        assertEquals(
            "点击检查是否有新版本",
            updateRowText(null, false, UpdateDownloadState.Idle, "2.5.3"),
        )
        assertEquals(
            "已是最新版本 2.5.3",
            updateRowText(UpdateChecker.Result.UpToDate("2.5.3"), false, UpdateDownloadState.Idle, "2.5.3"),
        )
        assertEquals(
            "发现新版本 2.5.4，点一下直接下载",
            updateRowText(
                UpdateChecker.Result.Newer("2.5.4", "2.5.3"),
                false,
                UpdateDownloadState.Idle,
                "2.5.3",
            ),
        )
    }
}
