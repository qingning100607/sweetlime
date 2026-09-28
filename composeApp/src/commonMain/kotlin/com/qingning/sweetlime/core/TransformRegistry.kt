package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.styles.extendedStyles
import com.qingning.sweetlime.core.styles.latinStyles
import com.qingning.sweetlime.core.styles.overlayStyles

/**
 * 样式注册表 —— 全应用唯一的样式来源。
 * 想加新样式：写一个 [TextTransform] 丢进对应的 styles 文件即可，其余代码无需改动。
 */
object TransformRegistry {
    val all: List<TextTransform> = latinStyles + overlayStyles + extendedStyles

    /** 按分组归好类的样式，顺序与 [StyleGroup] 声明顺序一致。 */
    val grouped: List<Pair<StyleGroup, List<TextTransform>>> =
        StyleGroup.entries
            .map { group -> group to all.filter { it.group == group } }
            .filter { (_, styles) -> styles.isNotEmpty() }

    fun byId(id: String): TextTransform? = all.firstOrNull { it.id == id }
}
