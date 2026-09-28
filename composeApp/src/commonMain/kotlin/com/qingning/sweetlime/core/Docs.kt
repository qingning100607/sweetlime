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

/** 开源许可。 */
object OpenSourceLicenses {
    const val TITLE: String = "开源许可"

    val SECTIONS: List<Doc> = listOf(
        Doc(
            "Kotlin & Compose Multiplatform",
            "Copyright JetBrains s.r.o. 与 The Android Open Source Project\n" +
                "License: Apache License 2.0",
        ),
        Doc(
            "compose-miuix-ui（miuix）",
            "Copyright compose-miuix-ui contributors\nLicense: Apache License 2.0",
        ),
        Doc(
            "AndroidX",
            "Copyright The Android Open Source Project\nLicense: Apache License 2.0",
        ),
        Doc(
            "数据来源",
            "样式码表、44 类特殊符号、汉字拼音、拆字数据由公开网络资料整理汇编，" +
                "仅供学习与交流使用。如果其中包含你的作品且不希望被收录，" +
                "请联系作者，会尽快移除。",
        ),
        Doc(
            "Apache License 2.0 摘要",
            "在遵守以下条件的前提下，允许自由使用、修改和分发：" +
                "保留版权声明与许可声明；标明修改过的文件；" +
                "不得使用原作者名号为衍生作品背书。软件按「原样」提供，" +
                "不附带任何明示或默示的担保。",
        ),
    )
}