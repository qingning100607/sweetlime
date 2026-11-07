package com.qingning.sweetlime.data

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import com.qingning.sweetlime.core.KeyValueStore

class FavoritesStore(private val store: KeyValueStore) {

    private val _keys: SnapshotStateList<String> = mutableStateListOf()

    private val _groups: SnapshotStateList<String> = mutableStateListOf()

    private val _groupOf = mutableStateMapOf<String, String>()

    private val _collapsed: SnapshotStateList<String> = mutableStateListOf()

    val keys: List<String> get() = _keys

    init {
        _keys.addAll(load())
        _groups.addAll(loadGroups())
        _groupOf.putAll(loadGroupMap())
        _collapsed.addAll(loadCollapsed())
    }

    val groups: List<String> get() = _groups

    fun addGroup(name: String): Boolean {
        val value = name.trim()
        if (value.isEmpty() || _groups.contains(value)) return false
        _groups.add(value)
        persistGroups()
        return true
    }

    fun removeGroup(name: String) {
        if (!_groups.remove(name)) return
        val affected = _groupOf.entries.filter { it.value == name }.map { it.key }
        affected.forEach { _groupOf.remove(it) }
        if (_collapsed.remove(name)) persistCollapsed()
        persistGroups()
        persistGroupMap()
    }

    fun isCollapsed(group: String): Boolean = _collapsed.contains(encodeGroup(group))

    fun toggleCollapsed(group: String) {
        val token = encodeGroup(group)
        if (!_collapsed.remove(token)) _collapsed.add(token)
        persistCollapsed()
    }

    fun groupOf(key: String): String = _groupOf[key].orEmpty()

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

        const val KEY = "favorites"

        const val LEGACY_KEY = "favorites_legacy_set"

        const val SEP = "\u0002"

        const val KEY_GROUPS = "favorite_groups"

        const val KEY_GROUP_MAP = "favorite_group_map"

        const val KEY_COLLAPSED = "favorite_collapsed"

        const val UNNAMED_TOKEN = "\u0004"

        const val UNIT = "\u0003"
    }
}