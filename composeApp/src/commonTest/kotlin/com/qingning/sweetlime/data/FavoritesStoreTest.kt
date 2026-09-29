package com.qingning.sweetlime.data

import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.core.TransformItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 收藏顺序的回归测试。
 *
 * 旧实现用 StringSet 持久化，读回来只能按字典序排：新增时插在最前、重启后跑到中间，
 * 「最近收藏排最前」这个承诺就兑现不了。
 *
 * 注意断言一律比较 `keys.toList()`：`keys` 是 SnapshotStateList，
 * 它的 equals 是**引用比较**，直接比两个实例即使内容一样也会判不等。
 */
class FavoritesStoreTest {

    /** 内存版 KeyValueStore。按类型分开存，模拟 SharedPreferences 的行为。 */
    private class FakeStore : KeyValueStore {
        private val strings = mutableMapOf<String, String>()
        private val sets = mutableMapOf<String, Set<String>>()

        override fun getString(key: String, defaultValue: String): String =
            strings[key] ?: defaultValue

        override fun putString(key: String, value: String) {
            strings[key] = value
        }

        override fun getStringSet(key: String): Set<String> = sets[key] ?: emptySet()

        override fun putStringSet(key: String, values: Set<String>) {
            sets[key] = values
        }

        override fun getBoolean(key: String, defaultValue: Boolean) = defaultValue
        override fun putBoolean(key: String, value: Boolean) = Unit
        override fun getInt(key: String, defaultValue: Int) = defaultValue
        override fun putInt(key: String, value: Int) = Unit
    }

    private fun favKey(styleId: String, input: String) = TransformItem(styleId, "", input, "").key

    @Test
    fun orderSurvivesReload() {
        val store = FakeStore()
        val favorites = FavoritesStore(store)
        favorites.toggle(favKey("latin_bold", "苹果"))
        favorites.toggle(favKey("latin_italic", "香蕉"))
        favorites.toggle(favKey("overlay_underline", "樱桃"))

        // 最新收藏的排最前。
        assertEquals(
            listOf(
                favKey("overlay_underline", "樱桃"),
                favKey("latin_italic", "香蕉"),
                favKey("latin_bold", "苹果"),
            ),
            favorites.keys.toList(),
        )

        // 重开一次（模拟杀进程后重启），顺序必须原样保留。
        val reopened = FavoritesStore(store)
        assertEquals(favorites.keys.toList(), reopened.keys.toList())
    }

    @Test
    fun togglingMovesExistingKeyToFront() {
        val store = FakeStore()
        val favorites = FavoritesStore(store)
        favorites.toggle(favKey("latin_bold", "苹果"))
        favorites.toggle(favKey("latin_italic", "香蕉"))
        // 再取消一次又收藏：先移除，再插到最前。
        favorites.toggle(favKey("latin_bold", "苹果"))
        favorites.toggle(favKey("latin_bold", "苹果"))

        assertEquals(
            listOf(favKey("latin_bold", "苹果"), favKey("latin_italic", "香蕉")),
            favorites.keys.toList(),
        )
    }

    @Test
    fun migratesLegacyStringSet() {
        val store = FakeStore()
        // 旧版本（≤2.6.8）存的 key 与格式。顺序已经不可逆地丢了，只要求不丢数据。
        store.putStringSet(
            "favorites_legacy_set",
            setOf(favKey("latin_bold", "苹果"), favKey("latin_italic", "香蕉")),
        )

        val favorites = FavoritesStore(store)
        assertEquals(2, favorites.keys.size)
        assertTrue(favorites.contains(favKey("latin_bold", "苹果")))

        // 迁移后旧 key 应被清空，新格式里能读到同样的内容。
        assertTrue(store.getStringSet("favorites_legacy_set").isEmpty())
        assertEquals(favorites.keys.toList(), FavoritesStore(store).keys.toList())
    }

    @Test
    fun keyWithSeparatorIsNotSplit() {
        // 收藏 key 本身带 \u0001，条目分隔符必须用别的字符，否则 key 会被劈开。
        val store = FakeStore()
        val favorites = FavoritesStore(store)
        val key = favKey("latin_bold", "a\u0001b")
        favorites.toggle(key)

        assertEquals(listOf(key), favorites.keys.toList())
        assertEquals(listOf(key), FavoritesStore(store).keys.toList())
    }
}