package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.styles.extendedStyles
import com.qingning.sweetlime.core.styles.latinStyles
import com.qingning.sweetlime.core.styles.overlayStyles

/**
 * 样式注册表 —— 全应用唯一的样式来源。
 * 想加新样式：写一个 [TextTransform] 丢进对应的 styles 文件即可，其余代码无需改动。
 */
object TransformRegistry {
    /**
     * 全部样式。
     *
     * 这里是 `val`，样式对象只构造一次 —— 之所以切语言还能生效，是因为
     * [TextTransform.title] 是「读时才翻译」的 getter，而不是构造时定死的字符串。
     */
    val all: List<TextTransform> = latinStyles + overlayStyles + extendedStyles

    /** 按分组归好类的样式，顺序与 [StyleGroup] 声明顺序一致。 */
    val grouped: List<Pair<StyleGroup, List<TextTransform>>> =
        StyleGroup.entries
            .map { group -> group to all.filter { it.group == group } }
            .filter { (_, styles) -> styles.isNotEmpty() }

    /**
     * id → 样式。样式对象是稳定的（语言不影响对象本身），所以可以安全建索引，
     * 把 [byId] 从每次线性扫描 O(n) 降到 O(1) —— 收藏页 / 最近使用 / 分类页都在循环里调它。
     */
    private val byIdIndex: Map<String, TextTransform> by lazy { all.associateBy { it.id } }

    fun byId(id: String): TextTransform? = byIdIndex[id]
}
