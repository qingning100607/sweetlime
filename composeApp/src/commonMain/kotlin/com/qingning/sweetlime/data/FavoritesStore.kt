package com.qingning.sweetlime.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import com.qingning.sweetlime.core.KeyValueStore

/**
 * 收藏夹。以 [com.qingning.sweetlime.core.TransformItem.key] 形式持久化，
 * 展示时再按样式重新计算输出。
 */
class FavoritesStore(private val store: KeyValueStore) {

    private val _keys: SnapshotStateList<String> = mutableStateListOf()

    val keys: List<String> get() = _keys

    init {
        _keys.addAll(store.getStringSet(KEY).sorted())
    }

    fun contains(key: String): Boolean = _keys.contains(key)

    fun toggle(key: String) {
        if (_keys.contains(key)) {
            _keys.remove(key)
        } else {
            _keys.add(0, key)
        }
        persist()
    }

    fun remove(key: String) {
        _keys.remove(key)
        persist()
    }

    fun clear() {
        _keys.clear()
        persist()
    }

    private fun persist() {
        store.putStringSet(KEY, _keys.toSet())
    }

    private companion object {
        const val KEY = "favorites"
    }
}