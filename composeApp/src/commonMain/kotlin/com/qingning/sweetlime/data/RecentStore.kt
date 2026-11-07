package com.qingning.sweetlime.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.qingning.sweetlime.core.KeyValueStore

class RecentStore(private val store: KeyValueStore) {

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

        const val SEP = "\u0001"
    }
}
