package com.qingning.sweetlime.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
    // 用户自建的分组（只存名字，顺序就是展示顺序）。
    private val _groups: SnapshotStateList<String> = mutableStateListOf()
    // 某条收藏属于哪个分组；没有记录 = 未分组。
    private val _groupOf = mutableStateMapOf<String, String>()
    // 处于收起状态的分组（默认全部展开）。跟分组本身一起持久化。
    private val _collapsed: SnapshotStateList<String> = mutableStateListOf()

    val keys: List<String> get() = _keys

    init {
        _keys.addAll(load())
        _groups.addAll(loadGroups())
        _groupOf.putAll(loadGroupMap())
        _collapsed.addAll(loadCollapsed())
    }

    /** 自建分组（按创建顺序）。 */
    val groups: List<String> get() = _groups

    /** 新增分组：名字去空格后不能为空、也不能重名。 */
    fun addGroup(name: String): Boolean {
        val value = name.trim()
        if (value.isEmpty() || _groups.contains(value)) return false
        _groups.add(value)
        persistGroups()
        return true
    }

    /** 删除分组：组里的收藏退回「未分组」。 */
    fun removeGroup(name: String) {
        if (!_groups.remove(name)) return
        val affected = _groupOf.entries.filter { it.value == name }.map { it.key }
        affected.forEach { _groupOf.remove(it) }
        if (_collapsed.remove(name)) persistCollapsed()
        persistGroups()
        persistGroupMap()
    }

    /** 这个分组当前是不是收起的。 */
    fun isCollapsed(group: String): Boolean = _collapsed.contains(encodeGroup(group))

    /** 展开 / 收起一个分组，并记住这个状态（下次进来保持原样）。 */
    fun toggleCollapsed(group: String) {
        val token = encodeGroup(group)
        if (!_collapsed.remove(token)) _collapsed.add(token)
        persistCollapsed()
    }

    /** 某条收藏所属的分组；空串 = 未分组。 */
    fun groupOf(key: String): String = _groupOf[key].orEmpty()

    /** 把一条收藏挪到某个分组；传空串等于挪回未分组。 */
    fun moveTo(key: String, group: String) {
        if (group.isEmpty()) {
            _groupOf.remove(key)
        } else {
            _groupOf[key] = group
        }
        persistGroupMap()
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
        if (_groupOf.remove(key) != null) persistGroupMap()
        persist()
    }

    fun clear() {
        _keys.clear()
        _groupOf.clear()
        persist()
        persistGroupMap()
    }

    private fun persist() {
        store.putString(KEY, _keys.joinToString(SEP))
    }

    private fun persistGroups() {
        store.putString(KEY_GROUPS, _groups.joinToString(SEP))
    }

    private fun persistCollapsed() {
        store.putString(KEY_COLLAPSED, _collapsed.joinToString(SEP))
    }

    private fun persistGroupMap() {
        store.putString(
            KEY_GROUP_MAP,
            _groupOf.entries.joinToString(SEP) { it.key + UNIT + it.value },
        )
    }

    private fun loadCollapsed(): List<String> =
        store.getString(KEY_COLLAPSED, "").split(SEP).filter { it.isNotEmpty() }

    /** 「未分组」的名字是空串，不能直接当持久化标记用，换成 [UNNAMED_TOKEN]。 */
    private fun encodeGroup(group: String): String =
        if (group.isEmpty()) UNNAMED_TOKEN else group

    private fun loadGroups(): List<String> =
        store.getString(KEY_GROUPS, "").split(SEP).filter { it.isNotEmpty() }

    private fun loadGroupMap(): Map<String, String> {
        val raw = store.getString(KEY_GROUP_MAP, "")
        if (raw.isEmpty()) return emptyMap()
        return buildMap {
            raw.split(SEP).forEach { entry ->
                val at = entry.indexOf(UNIT)
                if (at > 0) put(entry.substring(0, at), entry.substring(at + 1))
            }
        }
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

        /** 分组名存这里（有序串）。 */
        const val KEY_GROUPS = "favorite_groups"

        /** 「收藏 key → 分组名」的映射，条目之间用 [SEP]，键值之间用 [UNIT]。 */
        const val KEY_GROUP_MAP = "favorite_group_map"

        /** 收起状态存这里（分组名有序串）。 */
        const val KEY_COLLAPSED = "favorite_collapsed"

        /** 「未分组」在收起记录里的占位标记。 */
        const val UNNAMED_TOKEN = "\u0004"

        /** 映射里键和值之间的分隔符。 */
        const val UNIT = "\u0003"
    }
}