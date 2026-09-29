package com.qingning.sweetlime.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import com.qingning.sweetlime.core.KeyValueStore

/**
 * 收藏夹。以 [com.qingning.sweetlime.core.TransformItem.key] 形式持久化，
 * 展示时再按样式重新计算输出。
 *
 * 顺序是有意义的（最近收藏的排最前），所以持久化用**有序串**而不是 StringSet：
 * Set 无序，读回来只能按字典序排，重启一次收藏顺序就被打散了。
 */
class FavoritesStore(private val store: KeyValueStore) {

    private val _keys: SnapshotStateList<String> = mutableStateListOf()

    val keys: List<String> get() = _keys

    init {
        _keys.addAll(load())
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
        store.putString(KEY, _keys.joinToString(SEP))
    }

    /**
     * 读收藏。
     *
     * 新格式是有序串。老版本（≤2.6.8）存在 [LEGACY_KEY] 里、是个无序 StringSet，
     * 顺序已经不可逆地丢了，只能按字典序还原一次（至少和旧版重启后的表现一致），
     * 同时写成新格式迁移过来，下次启动就正常了。
     *
     * 迁移必须换 key：SharedPreferences 的 getString / getStringSet 遇到
     * 类型不符会直接 ClassCastException，同一个 key 不能先存 Set 再读 String。
     */
    private fun load(): List<String> {
        val raw = store.getString(KEY, "")
        if (raw.isNotEmpty()) return raw.split(SEP).filter { it.isNotEmpty() }

        val legacy = store.getStringSet(LEGACY_KEY).sorted()
        if (legacy.isNotEmpty()) {
            store.putString(KEY, legacy.joinToString(SEP))
            store.putStringSet(LEGACY_KEY, emptySet())
        }
        return legacy
    }

    private companion object {
        /** 新格式（有序串）。 */
        const val KEY = "favorites"

        /** 旧格式（无序 StringSet），只在迁移时读一次。 */
        const val LEGACY_KEY = "favorites_legacy_set"

        /**
         * 条目分隔符。
         *
         * 不能和 [com.qingning.sweetlime.core.TransformItem.SEPARATOR] 用同一个字符：
         * 收藏 key 本身就是 `styleId + \u0001 + input`，再用 \u0001 分隔条目会把 key 劈开。
         */
        const val SEP = "\u0002"
    }
}