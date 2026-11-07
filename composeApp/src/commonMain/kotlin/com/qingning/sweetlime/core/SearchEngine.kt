package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf
import com.qingning.sweetlime.core.mapping.CHAI_MAP
import com.qingning.sweetlime.core.mapping.PINYIN_MAP
import com.qingning.sweetlime.core.mapping.SYMBOL_CATEGORIES

sealed interface SearchHit {

    val title: String

    val summary: String

    data class Style(
        val transform: TextTransform,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    data class StyleCategory(
        val groupId: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    data class Symbol(
        val category: String,
        val symbol: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    data class SymbolCategory(
        val index: Int,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    data class Hanzi(
        val char: Char,

        val payload: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit

    data class Tool(
        val id: String,
        override val title: String,
        override val summary: String,
    ) : SearchHit
}

object SearchEngine {

    private const val MAX_STYLES = 60

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
                        summary = parts.joinToString(" · ") + tr("（点击复制）"),
                    ),
                )
            }
        }

        val matchedGroups = LinkedHashSet<StyleGroup>()
        for (style in TransformRegistry.all) {
            val score = styleScore(style, q, lqId) ?: continue
            if (score == CATEGORY_ONLY) {
                matchedGroups.add(style.group)
                continue
            }
            val preview = TransformItem.of(style, input)?.output?.listPreview().orEmpty()
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

        matchedGroups.forEach { group ->
            styleCategories.add(
                SearchHit.StyleCategory(
                    groupId = group.name,
                    title = group.label,
                    summary = tr("分类 · 点进去看这一类下的全部样式"),
                ),
            )
        }

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
                        summary = trf("{} · 点击复制", category.title),
                    ),
                )
                if (symbols.size >= MAX_SYMBOLS) break@outer
            }
        }

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

    private const val CATEGORY_ONLY = 1

    private fun styleScore(style: TextTransform, q: String, lqId: String): Int? {
        val name = style.title.trimEnd { it.isDigit() }
        val query = q.trimEnd { it.isDigit() }
        if (query.isNotEmpty() && name.isNotEmpty()) {
            if (name.equals(query, ignoreCase = true)) return 100
            if (name.startsWith(query, ignoreCase = true)) return 90
            if (name.contains(query, ignoreCase = true)) return 80
        }

        val id = style.id.filter { !it.isDigit() }
        val queryId = lqId.filter { !it.isDigit() }
        if (queryId.isNotEmpty() && id.contains(queryId)) return 30
        if (style.group.label.contains(q, ignoreCase = true)) return CATEGORY_ONLY
        return null
    }
}