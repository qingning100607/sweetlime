package com.qingning.sweetlime.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.qingning.sweetlime.core.KeyValueStore

/**
 * 「最近使用」的样式。
 *
 * 只存样式 id，不存结果 —— 展示时拿当前输入框里的文字重新算一遍，
 * 这样历史记录永远和当前输入同步，不会出现「存的是旧结果」的割裂感。
 * 最近用过的排在最前面，最多留 [MAX] 条。
 */
class RecentStore(private val store: KeyValueStore) {

    private var raw by mutableStateOf(store.getString(KEY, ""))

    val ids: List<String> get() = raw.split(SEP).filter { it.isNotEmpty() }

    fun record(styleId: String) {
        val list = ids.filter { it != styleId }.toMutableList()
        list.add(0, styleId)
        raw = list.take(MAX).joinToString(SEP)
        store.putString(KEY, raw)
    }

    fun clear() {
        raw = ""
        store.putString(KEY, "")
    }

    private companion object {
        const val KEY = "recent_styles"
        const val MAX = 12

        /** 用控制字符分隔，普通文本里不可能出现，所以不用做转义。 */
        const val SEP = "\u0001"
    }
}
