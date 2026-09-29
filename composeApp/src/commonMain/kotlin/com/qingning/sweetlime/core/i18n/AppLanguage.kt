package com.qingning.sweetlime.core.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * 界面语言。
 *
 * [code] 是存进 SharedPreferences 的值；[label] 是设置页弹窗里那一行字，
 * 一律用该语言自己的写法（中文就写中文、英文就写 English），不参与翻译。
 */
enum class AppLanguage(val code: String, val label: String) {
    ZH("zh", "简体中文"),
    EN("en", "English"),
    TW("tw", "繁體中文"),
    ;

    companion object {
        /** 从存储的 code 还原，认不出来就当简体。 */
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ZH
    }
}

/**
 * 当前语言。
 *
 * 用 Compose 状态承载：[tr] / [trf] 在组合期读它，所以切换语言后整个界面
 * 会自动重组，不需要重启 Activity。
 */
object AppLocale {
    var current: AppLanguage by mutableStateOf(AppLanguage.ZH)

    /** 启动时和切换时都走这里，保证内存状态与存储一致。 */
    fun apply(language: AppLanguage) {
        current = language
    }
}