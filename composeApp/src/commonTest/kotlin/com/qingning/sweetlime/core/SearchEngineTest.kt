package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertTrue

class SearchEngineTest {

    private val hua = "花"
    private val symbol = "符号"

    @Test
    fun emptyQueryGivesNothing() {
        assertTrue(SearchEngine.search("", "").isEmpty())
        assertTrue(SearchEngine.search("   ", "").isEmpty())
    }

    @Test
    fun findsStylesAndTheirCategories() {
        val hits = SearchEngine.search(hua, hua)
        assertTrue(hits.any { it is SearchHit.Style }, "should hit at least one style")
        assertTrue(
            hits.any { it is SearchHit.StyleCategory },
            "category names containing the keyword should give one entry each",
        )
        val style = hits.filterIsInstance<SearchHit.Style>().first()
        assertTrue(style.summary.isNotEmpty(), "style hits need a preview")
    }

    @Test
    fun singleHanziCarriesPinyin() {
        val hits = SearchEngine.search(hua, hua)
        assertTrue(hits.any { it is SearchHit.Hanzi })
    }

    @Test
    fun findsSymbolCategories() {
        val hits = SearchEngine.search(symbol, "")
        assertTrue(hits.isNotEmpty())
        assertTrue(hits.any { it is SearchHit.SymbolCategory })
    }

    @Test
    fun digitPrefixedQueryStaysEmpty() {

        assertTrue(SearchEngine.search("1" + hua, "").isEmpty())
    }

    @Test
    fun findsToolById() {

        val hits = SearchEngine.search("\u6c47\u7387", "")
        assertTrue(hits.filterIsInstance<SearchHit.Tool>().any { it.id == "currency" })
    }

    @Test
    fun respectsLimit() {
        assertTrue(SearchEngine.search(symbol, "", limit = 5).size <= 5)
    }
}
