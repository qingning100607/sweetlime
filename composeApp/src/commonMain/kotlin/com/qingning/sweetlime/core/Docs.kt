package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES

class Doc(val title: String, val body: String)

object PrivacyPolicy {
    val TITLE: String get() = tr("隐私政策")

    val SECTIONS: List<Doc>
    get() = listOf(
        Doc(
            tr("本机处理"),
            tr("花体、上下标、叠加符号、飞鸟文、拼音、拆字、金额大写等功能，") +
                tr("用的都是内置在安装包里的码表和字典，不请求任何接口。") +
                tr("你可以开飞行模式用一遍，效果完全一样。"),
        ),
        Doc(
            tr("剪贴板"),
            tr("只有在您主动点「复制」「粘贴」「分享」时才会读写剪贴板，") +
                tr("不会在后台监听剪贴板内容，也不会把它写入任何地方。"),
        ),
        Doc(
            tr("本地存储"),
            tr("收藏、最近使用、外观设置保存在应用私有目录里（SharedPreferences），") +
                tr("其他应用读不到，卸载应用就会一并删除。"),
        ),
        Doc(
            tr("网络与跳转"),
            tr("只有两处会连接网络：「检查更新」（仅在你主动点击时，") +
                tr("只读取一个版本号文本）和「一键加群」（交给 QQ 打开）。") +
                tr("两处都不携带任何个人数据，也没有埋点、统计和广告 SDK。"),
        ),
        Doc(
            tr("权限"),
            tr("只声明了网络权限（用于检查更新）。不申请通讯录、定位、相机、存储等权限；") +
                tr("分享用的是系统分享面板，由你选择目标应用。"),
        ),
    )
}

object OpenSourceLicenses {
    val TITLE: String get() = tr("开源许可")

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
            "miuix-glass (glass surfaces)",
            "Copyright compose-miuix-ui contributors / Halcyon contributors\n" +
                "License: Apache License 2.0\n\n" +
                "Used for the OS4 glass dropdown popups and the glass capsule " +
                "navigation bar. Source files were taken from the Apache-2.0 " +
                "licensed miuix-glass package and adapted.",
        ),
        Doc(
            "Haze",
            "Copyright Chris Banes\nLicense: Apache License 2.0\nhttps://github.com/chrisbanes/haze\n\nUsed for the live frosted glass on the top bar, the bottom bar and the\nfloating capsule (HazeState / hazeSource / hazeEffect with a progressive\nvertical gradient). No source files were copied; only the published API\nis used.",
        ),
        Doc(
            "AndroidX (activity-compose)",
            "Copyright The Android Open Source Project\nLicense: Apache License 2.0",
        ),
        Doc(
            "Data Sources",
            "Style code tables, " + SYMBOL_CATEGORIES.size + " categories of special characters, Chinese Pinyin and\n" +
                "character-decomposition data were compiled from publicly available sources\n" +
                "and are provided for learning and reference only. If your work is included\n" +
                "here and you would prefer it not to be, please contact the author and it\n" +
                "will be removed as soon as possible.",
        ),
        Doc(
            "Kotlin Coroutines",
            "Copyright JetBrains s.r.o. and contributors\nLicense: Apache License 2.0",
        ),
        Doc(
            "androidx.navigationevent",
            "Copyright The Android Open Source Project\nLicense: Apache License 2.0",
        ),
        Doc(
            "miuix squircle / blur / nav",
            "Copyright compose-miuix-ui contributors\nLicense: Apache License 2.0",
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