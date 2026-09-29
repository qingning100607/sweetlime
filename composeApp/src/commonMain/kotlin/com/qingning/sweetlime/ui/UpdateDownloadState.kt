package com.qingning.sweetlime.ui

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.UpdateChecker

/**
 * 「查到新版本 → 在应用内直接下载 → 唤起系统安装器」这条链路的进度状态。
 *
 * 主页顶部的提示和设置页的「检查更新」共用同一份状态，所以两边看到的东西永远一致：
 * 在主页点了下载，进设置页也还是「正在下载 42%」，不会各说各话。
 */
sealed interface UpdateDownloadState {

    /** 还没开始下载。 */
    data object Idle : UpdateDownloadState

    /**
     * 正在下载。
     *
     * [progress] 是 0f..1f；服务器没给 Content-Length 时是 -1f（进度未知，只能说"下载中"）。
     */
    data class Downloading(val progress: Float) : UpdateDownloadState

    /** 下好了，[path] 是应用私有目录里的本地文件。 */
    data class Ready(val path: String) : UpdateDownloadState

    /**
     * 包已经下好，但系统还没给「安装未知应用」权限。
     *
     * 第一次装的时候基本都会走到这里（点安装就等于"点了没反应"），
     * 所以单独标一个状态：界面明确告诉用户"点一下去授权"，而不是干等。
     */
    data class NeedsPermission(val path: String) : UpdateDownloadState

    /** 下载失败（断网 / 源没了 / 写盘失败）。 */
    data object Failed : UpdateDownloadState
}

/** 下载进度 → 给界面看的文案（拿不到总长度时进度为负）。 */
fun downloadProgressLabel(progress: Float): String = when {
    progress < 0f -> tr("正在下载…")
    progress >= 1f -> tr("下载完成，正在打开安装…")
    else -> trf("正在下载 {}%", (progress * 100).toInt())
}

/** 主页顶部横幅上的字：标题 + 一行说明，失败时再多一行「网络怎么办」的提示。 */
data class UpdateBannerText(
    val title: String,
    val summary: String,
    val hint: String? = null,
)

/**
 * 主页顶部横幅显示什么（拿 [version] 和当前下载状态推出来，纯函数，好测）。
 */
fun updateBannerText(version: String, state: UpdateDownloadState): UpdateBannerText = when (state) {
    is UpdateDownloadState.Downloading ->
        UpdateBannerText(trf("正在下载 {}", version), downloadProgressLabel(state.progress))

    is UpdateDownloadState.Ready ->
        UpdateBannerText(trf("安装 {}", version), tr("已下载好，点一下打开系统安装器"))

    is UpdateDownloadState.NeedsPermission ->
        UpdateBannerText(trf("安装 {}", version), tr("需先允许「安装未知应用」，点一下授权"))

    UpdateDownloadState.Failed ->
        UpdateBannerText(
            trf("发现新版本 {}", version),
            tr("下载失败，点一下重试"),
            // 失败十有八九是网络：GitHub 在国内经常连不上/半路断流（实测 TLS 都会掉），
            // 所以这里直接给一条「怎么办」，而不是让用户对着「重试」反复撞墙。
            tr("下载失败了？国内直连 GitHub 经常抽风（更新源就在 GitHub）。开个代理 / 加速再点一下重试，或者到 GitHub 仓库页手动下载安装包。"),
        )

    UpdateDownloadState.Idle ->
        UpdateBannerText(trf("发现新版本 {}", version), tr("点一下在应用内直接下载"))
}

/**
 * 设置页「检查更新」那一行的说明文字。
 *
 * 文案优先级：正在检查 / 下载中 / 下载失败 / 已下载待安装 → 检查结果 → 默认提示。
 * 这样下载过程中即使再点一次「检查更新」，行上也不会突然跳回「点击检查是否有新版本」。
 */
fun updateRowText(
    result: UpdateChecker.Result?,
    checking: Boolean,
    state: UpdateDownloadState,
    currentVersion: String,
): String = when {
    checking -> tr("正在检查…")
    state is UpdateDownloadState.Downloading -> downloadProgressLabel(state.progress)
    state is UpdateDownloadState.Ready -> tr("已下载好，点一下打开系统安装器")
    state is UpdateDownloadState.NeedsPermission -> tr("需先允许「安装未知应用」，点一下授权")
    state is UpdateDownloadState.Failed -> tr("下载失败，点一下重试")
    result is UpdateChecker.Result.UpToDate -> trf("已是最新版本 {}", currentVersion)
    result is UpdateChecker.Result.NotConfigured -> trf("当前版本 {}（更新源未配置）", currentVersion)
    result is UpdateChecker.Result.Newer -> trf("发现新版本 {}，点一下直接下载", result.latest)
    result is UpdateChecker.Result.Failed -> tr("检查失败，请稍后再试")
    else -> tr("点击检查是否有新版本")
}
