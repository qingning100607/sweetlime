package com.qingning.sweetlime.core.i18n

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.systemUiLanguage

enum class AppLanguage(val code: String, val label: String) {
    ZH("zh", "简体中文"),
    EN("en", "English"),
    TW("tw", "繁體中文"),
    ;

    companion object {

        const val AUTO_CODE: String = "auto"

        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ZH

        fun resolve(code: String?, system: AppLanguage): AppLanguage =
            if (code.isNullOrBlank() || code == AUTO_CODE) system else fromCode(code)
    }
}

object AppLocale {

    var current: AppLanguage by mutableStateOf(AppLanguage.ZH)

    var selection: String by mutableStateOf(AppLanguage.ZH.code)

    fun apply(language: AppLanguage) {
        current = language
        selection = language.code
    }

    fun applyStored(code: String, system: AppLanguage = systemUiLanguage()) {
        selection = code
        current = AppLanguage.resolve(code, system)
    }
}