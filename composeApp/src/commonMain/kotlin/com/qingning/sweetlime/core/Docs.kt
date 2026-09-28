package com.qingning.sweetlime.core

/** 一段带标题的说明文字（隐私政策 / 开源许可共用这个形状）。 */
class Doc(val title: String, val body: String)

/**
 * 隐私政策。
 *
 * 这个应用是「纯本地工具」，所以这份政策的核心就一句话：
 * 你的文字不会离开这台设备。
 */
object PrivacyPolicy {
    const val TITLE: String = "隐私政策"

    val SECTIONS: List<Doc> = listOf(
        Doc(
            "本机处理",
            "花体、上下标、叠加符号、飞鸟文、拼音、拆字、金额大写等功能，" +
                "用的都是内置在安装包里的码表和字典，不请求任何接口。" +
                "你可以开飞行模式用一遍，效果完全一样。",
        ),
        Doc(
            "剪贴板",
            "只有在您主动点「复制」「粘贴」「分享」时才会读写剪贴板，" +
                "不会在后台监听剪贴板内容，也不会把它写入任何地方。",
        ),
        Doc(
            "本地存储",
            "收藏、最近使用、外观设置保存在应用私有目录里（SharedPreferences），" +
                "其他应用读不到，卸载应用就会一并删除。",
        ),
        Doc(
            "网络与跳转",
            "只有两处会连接网络：「检查更新」（仅在你主动点击时，" +
                "只读取一个版本号文本）和「一键加群」（交给 QQ 打开）。" +
                "两处都不携带任何个人数据，也没有埋点、统计和广告 SDK。",
        ),
        Doc(
            "权限",
            "只声明了网络权限（用于检查更新）。不申请通讯录、定位、相机、存储等权限；" +
                "分享用的是系统分享面板，由你选择目标应用。",
        ),
    )
}

/**
 * 开源许可。
 *
 * 这里的**正文一律用英文**：Apache-2.0 这类许可证要求署名连同许可条款一起保留，
 * 用中文转述容易被再分发方当成「改动了许可条款」。界面上的入口标题（设置页那一行）
 * 仍然是中文，本页的标题也一并给英文，和正文保持一致。
 */
object OpenSourceLicenses {
    const val TITLE: String = "Open Source Licenses"

    val SECTIONS: List<Doc> = listOf(
        Doc(
            "Kotlin & Compose Multiplatform",
            "Copyright JetBrains s.r.o. and The Android Open Source Project\n" +
                "License: Apache License 2.0",
        ),
        Doc(
            "compose-miuix-ui (miuix)",
            "Copyright compose-miuix-ui contributors\nLicense: Apache License 2.0",
        ),
        Doc(
            "lyricon",
            "Copyright 2026 Proify, Tomakino\n" +
                "License: Apache License 2.0\n" +
                "https://github.com/kifranei/lyricon\n\n" +
                "The \"Flowing Background\" option in Settings uses its AGSL shaders and\n" +
                "color presets (app/src/main/kotlin/io/github/proify/lyricon/app/compose/effect/).\n" +
                "Files taken from that project: OS2BgFrag.kt, OS3BgFrag.kt, BgEffectConfig.kt,\n" +
                "BgEffectPainter.kt, FrameTimeSeconds.kt, BgEffectBackground.kt,\n" +
                "HyperOsDetector.kt (ported as HyperOsStyle.kt).\n\n" +
                "Modified, as declared under section 4(b) of the Apache License 2.0:\n" +
                "- preference handling was removed; the enabled flag and the shader style are\n" +
                "  now passed in as parameters from the settings screen;\n" +
                "- HyperOsStyle.kt keeps only the HyperOS major-version lookup and the\n" +
                "  follow-system / OS 2 / OS 3 resolution, accesses android.os.* via\n" +
                "  reflection so it can live in commonMain, and drops the upstream\n" +
                "  \"draw nothing on HyperOS 1\" branch (the switch is binary here);\n" +
                "- package renamed to com.qingning.sweetlime.ui.effect;\n" +
                "- com.qingning.sweetlime.ui.effect.FlowingSurface.kt and\n" +
                "  com.qingning.sweetlime.ui.effect.FlowingLayer.kt are new work added by this\n" +
                "  project and are not part of the original repository. FlowingSurface.kt\n" +
                "  overrides the surfaceContainer family with the same alphas as upstream\n" +
                "  (AppComposable.kt) and hides the divider; FlowingLayer.kt lets a page paint\n" +
                "  the very same flow layer as its own opaque background, reusing the single\n" +
                "  shader brush of BgEffectBackground (the upstream About screen does the same\n" +
                "  with a second BgEffectBackground instance; here one instance is shared).",
        ),
        Doc(
            "AndroidX",
            "Copyright The Android Open Source Project\nLicense: Apache License 2.0",
        ),
        Doc(
            "Data Sources",
            "Style code tables, 44 categories of special characters, Chinese Pinyin and\n" +
                "character-decomposition data were compiled from publicly available sources\n" +
                "and are provided for learning and reference only. If your work is included\n" +
                "here and you would prefer it not to be, please contact the author and it\n" +
                "will be removed as soon as possible.",
        ),
        Doc(
            "Apache License 2.0 (Summary)",
            "You may freely use, modify and distribute this software provided that you:\n" +
                "- retain the copyright and license notices;\n" +
                "- state which files you modified;\n" +
                "- do not use the original authors' names to endorse your derivative work.\n" +
                "This is a summary only; the software is provided \"AS IS\", without\n" +
                "warranties or conditions of any kind, and the full license text governs.",
        ),
    )
}