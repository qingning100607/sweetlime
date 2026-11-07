package com.qingning.sweetlime.data

import com.qingning.sweetlime.core.KeyValueStore
import com.qingning.sweetlime.core.TransformItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FavoritesStoreTest {

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

        assertEquals(
            listOf(
                favKey("overlay_underline", "樱桃"),
                favKey("latin_italic", "香蕉"),
                favKey("latin_bold", "苹果"),
            ),
            favorites.keys.toList(),
        )

        val reopened = FavoritesStore(store)
        assertEquals(favorites.keys.toList(), reopened.keys.toList())
    }

    @Test
    fun togglingMovesExistingKeyToFront() {
        val store = FakeStore()
        val favorites = FavoritesStore(store)
        favorites.toggle(favKey("latin_bold", "苹果"))
        favorites.toggle(favKey("latin_italic", "香蕉"))

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

        store.putStringSet(
            "favorites_legacy_set",
            setOf(favKey("latin_bold", "苹果"), favKey("latin_italic", "香蕉")),
        )

        val favorites = FavoritesStore(store)
        assertEquals(2, favorites.keys.size)
        assertTrue(favorites.contains(favKey("latin_bold", "苹果")))

        assertTrue(store.getStringSet("favorites_legacy_set").isEmpty())
        assertEquals(favorites.keys.toList(), FavoritesStore(store).keys.toList())
    }

    @Test
    fun keyWithSeparatorIsNotSplit() {

        val store = FakeStore()
        val favorites = FavoritesStore(store)
        val key = favKey("latin_bold", "a\u0001b")
        favorites.toggle(key)

        assertEquals(listOf(key), favorites.keys.toList())
        assertEquals(listOf(key), FavoritesStore(store).keys.toList())
    }
}