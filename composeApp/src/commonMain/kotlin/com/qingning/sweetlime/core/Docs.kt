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

class LibraryLicense(
    val name: String,
    val artifactVersion: String? = null,
    val developers: String? = null,
    val description: String? = null,
    val licenses: String? = null,
    val url: String? = null,
)

object OpenSourceLicenses {
    val TITLE: String get() = tr("开放源代码")

    val LIBRARIES: List<LibraryLicense>
        get() = listOf(
            LibraryLicense(
                name = "Kotlin",
                artifactVersion = "2.4.10",
                developers = "JetBrains s.r.o.",
                description = "Kotlin compiler and standard library. All app code is written in Kotlin.",
                licenses = "Apache License 2.0",
                url = "https://github.com/JetBrains/kotlin",
            ),
            LibraryLicense(
                name = "Compose Multiplatform",
                artifactVersion = "1.12.1",
                developers = "JetBrains s.r.o.",
                description = "Declarative UI toolkit that renders every screen of this app.",
                licenses = "Apache License 2.0",
                url = "https://github.com/JetBrains/compose-multiplatform",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : ui",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Core UI components: app bars, cards, buttons, text and scaffolds.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : icons",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Icon set used across the interface.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : preference",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Settings rows, switches and dropdown preferences.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : blur",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Backdrop blur backend behind the glass surfaces and bars.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : nav",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Navigation display effects and page transitions.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "compose-miuix-ui : squircle",
                artifactVersion = "0.9.4",
                developers = "compose-miuix-ui contributors",
                description = "Squircle corner shapes for cards and panels.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "miuix-glass",
                developers = "compose-miuix-ui contributors / Halcyon contributors",
                description = "Glass shaders and materials for the OS4 dropdown popups and the " +
                    "capsule navigation bar. Adapted from the Apache-2.0 licensed miuix-glass sources.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "Haze",
                artifactVersion = "1.7.3",
                developers = "Chris Banes",
                description = "Live frosted blur on the top bar, the bottom bar and the floating capsule. " +
                    "Public API only, no source files copied.",
                licenses = "Apache License 2.0",
                url = "https://github.com/chrisbanes/haze",
            ),
            LibraryLicense(
                name = "AndroidX Activity Compose",
                artifactVersion = "1.13.0",
                developers = "The Android Open Source Project",
                description = "Hosts the Compose UI inside the Android activity.",
                licenses = "Apache License 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/activity",
            ),
            LibraryLicense(
                name = "AndroidX NavigationEvent",
                artifactVersion = "1.1.2",
                developers = "The Android Open Source Project",
                description = "Back gesture and predictive back events.",
                licenses = "Apache License 2.0",
            ),
            LibraryLicense(
                name = "Kotlin Coroutines",
                developers = "JetBrains s.r.o.",
                description = "Coroutines and Flow for background work and animations.",
                licenses = "Apache License 2.0",
                url = "https://github.com/Kotlin/kotlinx.coroutines",
            ),
            LibraryLicense(
                name = "Data Sources",
                developers = "SweetLime",
                description = "Style code tables, " + SYMBOL_CATEGORIES.size + " symbol categories, " +
                    "pinyin and character-breakdown data compiled from public sources, " +
                    "for reference only. Contact the author if you want your work removed.",
                licenses = "Public sources",
            ),
            LibraryLicense(
                name = "Apache License 2.0",
                developers = "Apache Software Foundation",
                description = "These libraries may be used, modified and redistributed if you keep the " +
                    "copyright and license notices, state which files you changed, and do not use the " +
                    "original authors' names to endorse your work. The software is provided as is, " +
                    "without warranty. This is a summary only; the full license text governs.",
                licenses = "Apache License 2.0",
                url = "https://www.apache.org/licenses/LICENSE-2.0",
            ),
        )
}
