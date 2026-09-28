package com.qingning.sweetlime.ui

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
    progress < 0f -> "正在下载…"
    progress >= 1f -> "下载完成，正在打开安装…"
    else -> "正在下载 ${(progress * 100).toInt()}%"
}

/** 主页顶部横幅上那两行字。 */
data class UpdateBannerText(val title: String, val summary: String)

/**
 * 主页顶部横幅显示什么（拿 [version] 和当前下载状态推出来，纯函数，好测）。
 */
fun updateBannerText(version: String, state: UpdateDownloadState): UpdateBannerText = when (state) {
    is UpdateDownloadState.Downloading ->
        UpdateBannerText("正在下载 $version", downloadProgressLabel(state.progress))

    is UpdateDownloadState.Ready ->
        UpdateBannerText("安装 $version", "已下载好，点一下打开系统安装器")

    is UpdateDownloadState.NeedsPermission ->
        UpdateBannerText("安装 $version", "需先允许「安装未知应用」，点一下授权")

    UpdateDownloadState.Failed ->
        UpdateBannerText("发现新版本 $version", "下载失败，点一下重试")

    UpdateDownloadState.Idle ->
        UpdateBannerText("发现新版本 $version", "点一下在应用内直接下载")
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
    checking -> "正在检查…"
    state is UpdateDownloadState.Downloading -> downloadProgressLabel(state.progress)
    state is UpdateDownloadState.Ready -> "已下载好，点一下打开系统安装器"
    state is UpdateDownloadState.NeedsPermission -> "需先允许「安装未知应用」，点一下授权"
    state is UpdateDownloadState.Failed -> "下载失败，点一下重试"
    result is UpdateChecker.Result.UpToDate -> "已是最新版本 $currentVersion"
    result is UpdateChecker.Result.NotConfigured -> "当前版本 $currentVersion（更新源未配置）"
    result is UpdateChecker.Result.Newer -> "发现新版本 ${result.latest}，点一下直接下载"
    result is UpdateChecker.Result.Failed -> "检查失败，请稍后再试"
    else -> "点击检查是否有新版本"
}
