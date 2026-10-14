package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 搜索覆盖回归：**新加的东西必须能被搜到**。
 *
 * 起因：用户问「新添的功能有没有添加到搜索那里可以搜到」。
 * 光靠肉眼翻一遍顶不住以后 —— 加功能时忘了登记（不管是忘了进 TOOL_ENTRIES，
 * 还是样式没进注册表），这里会直接红。
 *
 * 判据：用**它自己的名字**搜，结果里必须出现它自己。
 * 搜索用的名字是界面语言下的（测试里就是简体中文），所以这里不需要额外翻译。
 */
class SearchCoverageTest {

    @Test
    fun everyToolIsFindableByItsOwnName() {
        val misses = TOOL_ENTRIES
            .filter { tool ->
                SearchEngine.search(tool.title, "").none { it is SearchHit.Tool && it.id == tool.id }
            }
            .map { it.id }
        assertEquals(emptyList(), misses, "这些工具用名字搜不到（新加功能要记得能搜到）：$misses")
    }

    @Test
    fun toolIdsAreUnique() {
        val ids = TOOL_ENTRIES.map { it.id }
        val dup = ids.groupingBy { it }.eachCount().filterValues { it > 1 }
        assertEquals(emptyMap(), dup, "工具 id 撞车：$dup")
    }

    @Test
    fun everyStyleIsFindableByItsOwnName() {
        val misses = TransformRegistry.all
            .filter { style ->
                SearchEngine.search(style.title, "").none {
                    it is SearchHit.Style && it.transform.id == style.id
                }
            }
            .map { it.id }
        assertEquals(emptyList(), misses, "这些样式用名字搜不到：$misses")
    }

    @Test
    fun everyStyleGroupIsFindable() {
        val misses = StyleGroup.entries
            .filter { group ->
                SearchEngine.search(group.label, "").none {
                    it is SearchHit.StyleCategory || it is SearchHit.Style
                }
            }
            .map { it.name }
        assertEquals(emptyList(), misses, "这些样式分类搜不到：$misses")
    }

    @Test
    fun everySymbolCategoryIsFindableByItsName() {
        val misses = SYMBOL_CATEGORIES.map { it.title }.filter { title ->
            SearchEngine.search(title, "").none {
                it is SearchHit.SymbolCategory || it is SearchHit.Symbol
            }
        }
        assertEquals(emptyList(), misses, "这些符号分类搜不到：$misses")
    }

    @Test
    fun searchIsEmptyForBlankQuery() {
        assertTrue(SearchEngine.search("   ", "abc").isEmpty(), "空查询不该返回结果")
    }
}
