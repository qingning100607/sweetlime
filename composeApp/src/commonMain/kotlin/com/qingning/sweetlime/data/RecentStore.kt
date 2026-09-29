package com.qingning.sweetlime.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.qingning.sweetlime.core.KeyValueStore

/**
 * 「最近使用」的样式。
 *
 * 只存样式 id，不存结果 —— 展示时拿当前输入框里的文字重新算一遍，
 * 这样历史记录永远和当前输入同步，不会出现「存的是旧结果」的割裂感。
 * 最近用过的排在最前面，最多留 [MAX] 条。
 */
class RecentStore(private val store: KeyValueStore) {

    /**
     * 内存里就是有序的 [SnapshotStateList]：既是 Compose 观测量，
     * 也省掉了「每次访问都 split 一遍字符串」的开销（旧实现是 getter）。
     */
    private val _ids: SnapshotStateList<String> = mutableStateListOf()

    val ids: List<String> get() = _ids

    init {
        _ids.addAll(
            store.getString(KEY, "")
                .split(SEP)
                .filter { it.isNotEmpty() }
                .take(MAX),
        )
    }

    fun record(styleId: String) {
        _ids.remove(styleId)
        _ids.add(0, styleId)
        while (_ids.size > MAX) _ids.removeAt(_ids.lastIndex)
        persist()
    }

    fun clear() {
        _ids.clear()
        persist()
    }

    private fun persist() {
        store.putString(KEY, _ids.joinToString(SEP))
    }

    private companion object {
        const val KEY = "recent_styles"
        const val MAX = 12

        /** 用控制字符分隔，普通文本里不可能出现，所以不用做转义。 */
        const val SEP = "\u0001"
    }
}
