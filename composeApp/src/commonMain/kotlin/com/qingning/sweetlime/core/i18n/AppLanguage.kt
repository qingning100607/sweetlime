package com.qingning.sweetlime.core.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.systemUiLanguage

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
        /** 「跟随系统」的存储值。不在 [entries] 里 —— 它不是一个具体语言。 */
        const val AUTO_CODE: String = "auto"

        /** 从存储的 code 还原，认不出来就当简体。 */
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ZH

        /**
         * 把「存储里那个 code」解析成真正要用的语言。
         *
         * - `auto`（或空）→ 用 [system] 传进来的系统语言；
         * - 具体语言 code → 就用它，无视系统。
         */
        fun resolve(code: String?, system: AppLanguage): AppLanguage =
            if (code.isNullOrBlank() || code == AUTO_CODE) system else fromCode(code)
    }
}

/**
 * 当前语言。
 *
 * 用 Compose 状态承载：[tr] / [trf] 在组合期读它，所以切换语言后整个界面
 * 会自动重组，不需要重启 Activity。
 */
object AppLocale {
    /** 真正生效的语言（`auto` 时是解析后的系统语言）。 */
    var current: AppLanguage by mutableStateOf(AppLanguage.ZH)

    /** 设置里选的那一项：具体语言 code，或 [AppLanguage.AUTO_CODE]。 */
    var selection: String by mutableStateOf(AppLanguage.ZH.code)

    /** 启动时和切换时都走这里，保证内存状态与存储一致。 */
    fun apply(language: AppLanguage) {
        current = language
        selection = language.code
    }

    /**
     * 按「存的 code」生效：`auto` 时解析成 [system]（系统语言）。
     *
     * 启动和切换语言都走这里，保证内存状态与存储、系统设置三者一致。
     */
    fun applyStored(code: String, system: AppLanguage = systemUiLanguage()) {
        selection = code
        current = AppLanguage.resolve(code, system)
    }
}