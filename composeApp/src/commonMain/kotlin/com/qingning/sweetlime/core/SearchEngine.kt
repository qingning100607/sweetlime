package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.mapping.CHAI_MAP
import com.qingning.sweetlime.core.mapping.PINYIN_MAP
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES

/** 搜索命中的一条结果。 */
sealed interface SearchHit {
    /** 主标题。 */
    val title: String

    /** 副标题（预览 / 说明）。 */
    val summary: String

    /** 样式命中：点进去就是那个样式的详情页。 */
    data class Style(
        val transform: TextTransform,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    /**
     * 样式**分类**命中（搜「花体」「空白」这种分类名）：
     * 只给一条入口，点进去看这一类下的全部样式 —— 不把整组铺在结果里。
     */
    data class StyleCategory(
        val groupId: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    /** 符号命中：点一下直接复制。 */
    data class Symbol(
        val category: String,
        val symbol: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    /**
     * 符号**分类**命中（搜「花朵」「箭头」这种分类名）：
     * 同样只给一条入口。以前会把整组 40 个符号全倒在结果里，
     * 里面绝大多数和关键词毫无关系，看起来就是「搜出来的东西不相符」。
     */
    data class SymbolCategory(
        val index: Int,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    /** 汉字信息命中（拼音 / 拆分）。 */
    data class Hanzi(
        val char: Char,
        /** 点一下真正复制走的内容（拼音 / 拆分，多行）。 */
        val payload: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    /**
     * 工具命中：点进去就是那个工具页。
     *
     * 工具箱里有十条东西，全平铺在一页里，搜索能搜到就少翻一屏。
     */
    data class Tool(
        val id: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit
}

/**
 * 本地搜索。
 *
 * 一个输入框同时干两件事：它既是**关键词**（找样式 / 符号 / 汉字），
 * 也是**原文**（样式结果的预览、以及点进详情页后「原文」那一栏的内容都用它）——
 * 所以搜完之后不用再去主页重新敲一遍字。
 *
 * 什么算「命中」：
 * - **样式**：样式名（末尾编号不算，`花藤字1` 按「花藤字」比）或英文 id；
 * - **样式分类**：命中分类名时只给**一条入口**（搜「花体」→ 一条「英文花体」），
 *   不把整组样式铺开；
 * - **符号**：符号本身含关键词；
 * - **符号分类**：命中分类名时同样只给一条入口；
 * - **汉字**：单个汉字时给出拼音与拆分。
 *
 * 排序：完全匹配 > 前缀匹配 > 包含匹配；样式名 > 分类名 > id。
 */
object SearchEngine {

    /** 样式最多给这么多条，避免一次刷屏。 */
    private const val MAX_STYLES = 60

    /** 符号最多给这么多条，不然会淹掉样式结果。 */
    private const val MAX_SYMBOLS = 40

    fun search(query: String, input: String, limit: Int = 120): List<SearchHit> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val lqId = q.lowercase().replace(" ", "")

        val hanzi = ArrayList<SearchHit>(1)
        val styleCategories = ArrayList<SearchHit.StyleCategory>()
        val styles = ArrayList<Pair<Int, SearchHit.Style>>()
        val symbolCategories = ArrayList<SearchHit.SymbolCategory>()
        val symbols = ArrayList<SearchHit.Symbol>()

        // 1. 汉字信息：只有单字才有意义，而且只认「字典里真的有数据」的字。
        if (q.length == 1) {
            val ch = q[0]
            val pinyin = PINYIN_MAP[ch]
            val chai = CHAI_MAP[ch]
            if (pinyin != null || chai != null) {
                val parts = buildList {
                    pinyin?.let { add(trf("拼音 {}", it)) }
                    chai?.let { add(trf("拆分 {}", it)) }
                }
                hanzi.add(
                    SearchHit.Hanzi(
                        char = ch,
                        payload = parts.joinToString("\n"),
                        title = "$ch",
                        summary = parts.joinToString(" · ") + tr("（点一下复制）"),
                    ),
                )
            }
        }

        // 2. 样式：名字 / id 命中就进结果列表；只命中分类名的话收成一条分类入口。
        val matchedGroups = LinkedHashSet<StyleGroup>()
        for (style in TransformRegistry.all) {
            val score = styleScore(style, q, lqId) ?: continue
            if (score == CATEGORY_ONLY) {
                matchedGroups.add(style.group)
                continue
            }
            val preview = TransformItem.of(style, input)?.output?.replace('\n', ' ').orEmpty()
            styles.add(
                score to SearchHit.Style(
                    transform = style,
                    title = style.title,
                    summary = when {
                        preview.isNotEmpty() -> preview
                        input.isBlank() -> style.group.label
                        else -> tr("这段文字在这里没有效果")
                    },
                ),
            )
        }
        styles.sortByDescending { it.first }
        // 分类入口排在同级样式前面。
        matchedGroups.forEach { group ->
            styleCategories.add(
                SearchHit.StyleCategory(
                    groupId = group.name,
                    title = group.label,
                    summary = tr("分类 · 点进去看这一类下的全部样式"),
                ),
            )
        }

        // 3. 符号：符号本身命中就给符号；只命中分类名的，收成一条分类入口。
        outer@ for ((index, category) in SYMBOL_CATEGORIES.withIndex()) {
            if (category.title.contains(q, ignoreCase = true)) {
                symbolCategories.add(
                    SearchHit.SymbolCategory(
                        index = index,
                        title = category.title,
                        summary = trf("分类 · {} 个符号，点进去慢慢挑", category.symbols.size),
                    ),
                )
                continue
            }
            for (symbol in category.symbols) {
                if (!symbol.contains(q, ignoreCase = true)) continue
                symbols.add(
                    SearchHit.Symbol(
                        category = category.title,
                        symbol = symbol,
                        title = symbol,
                        summary = trf("{} · 点一下复制", category.title),
                    ),
                )
                if (symbols.size >= MAX_SYMBOLS) break@outer
            }
        }

        // 4. 工具：搜「汇率」「Base64」这类词，工具箱里的条目也该出现。
        val tools = ArrayList<SearchHit.Tool>()
        for (tool in TOOL_ENTRIES) {
            if (!tool.title.contains(q, ignoreCase = true) &&
                !tool.summary.contains(q, ignoreCase = true)
            ) {
                continue
            }
            tools.add(
                SearchHit.Tool(
                    id = tool.id,
                    title = tool.title,
                    summary = trf("工具 · {}", tool.summary),
                ),
            )
        }
        val result = ArrayList<SearchHit>(limit)
        result.addAll(hanzi)
        result.addAll(tools)
        result.addAll(styleCategories)
        styles.take(MAX_STYLES).forEach { result.add(it.second) }
        result.addAll(symbolCategories)
        result.addAll(symbols)
        return if (result.size > limit) result.subList(0, limit) else result
    }

    /** 只命中分类名（样式名与 id 都没命中）时返回这个分数。 */
    private const val CATEGORY_ONLY = 1

    /**
     * 给一个样式的匹配强度打分；不命中返回 null。
     *
     * 100 完全等于样式名；90 样式名前缀；80 样式名包含；30 英文 id 包含；
     * 1 只命中分类名（调用方会把它收成一条分类入口）。
     *
     * **匹配前先去掉编号**：样式名末尾的数字（花藤字1 / 菊花文2 / tx_9）只是用来区分
     * 同名样式的序号，本身没有任何含义；不处理的话搜「1」会把所有带编号的样式全砸出来。
     */
    private fun styleScore(style: TextTransform, q: String, lqId: String): Int? {
        val name = style.title.trimEnd { it.isDigit() }
        val query = q.trimEnd { it.isDigit() }
        if (query.isNotEmpty() && name.isNotEmpty()) {
            if (name.equals(query, ignoreCase = true)) return 100
            if (name.startsWith(query, ignoreCase = true)) return 90
            if (name.contains(query, ignoreCase = true)) return 80
        }
        // id 是英文 snake_case（latin_bold_circled），去掉下划线与数字后按英文关键词匹配。
        val id = style.id.filter { !it.isDigit() }
        val queryId = lqId.filter { !it.isDigit() }
        if (queryId.isNotEmpty() && id.contains(queryId)) return 30
        if (style.group.label.contains(q, ignoreCase = true)) return CATEGORY_ONLY
        return null
    }
}