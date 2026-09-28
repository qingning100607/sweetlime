package com.qingning.sweetlime.ui.components

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * [pageChunks] 是二级页「长目录切块」的唯一逻辑：把 N 条切成每 per 条一段。
 *
 * 它一旦切错（丢条 / 重复 / 段号撞车），LazyColumn 的 key 就会重复或漏项，
 * 轻则条目错乱、重则直接抛「键重复」崩掉，所以这里把边界都钉死。
 */
class PageChunksTest {

    private fun chunk(count: Int, per: Int = 6): List<IntRange> = pageChunks(count, per)

    @Test
    fun `空列表不产生分块`() {
        assertEquals(emptyList<IntRange>(), chunk(0))
    }

    @Test
    fun `不足一块时就是一块`() {
        assertEquals(listOf(0 until 3), chunk(3))
    }

    @Test
    fun `正好整块`() {
        assertEquals(listOf(0 until 6, 6 until 12), chunk(12))
    }

    @Test
    fun `多出来的零头单独成块`() {
        // 13 条按每 6 条切：6 + 6 + 1，最后那块只有 1 条。
        assertEquals(listOf(0 until 6, 6 until 12, 12 until 13), chunk(13))
    }

    @Test
    fun `非法参数返回空`() {
        assertEquals(emptyList<IntRange>(), chunk(5, per = 0))
        assertEquals(emptyList<IntRange>(), chunk(-1))
    }

    @Test
    fun `45 个分类全部覆盖且不重复`() {
        val n = 45
        assertEquals((0 until n).toList(), chunk(n).flatMap { it.toList() })
    }

    @Test
    fun `每块起点就是 key 用的 first，互不相同`() {
        val starts = chunk(45).map { it.first }
        assertEquals(starts.distinct(), starts)
        assertEquals(listOf(0, 6, 12, 18, 24, 30, 36, 42), starts)
    }
}
