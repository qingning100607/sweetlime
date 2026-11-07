package com.qingning.sweetlime.core

import com.qingning.sweetlime.core.styles.aliasStyles
import com.qingning.sweetlime.core.styles.extendedStyles
import com.qingning.sweetlime.core.styles.latinStyles
import com.qingning.sweetlime.core.styles.overlayStyles

object TransformRegistry {

    val all: List<TextTransform> = latinStyles + overlayStyles + extendedStyles + aliasStyles

    val grouped: List<Pair<StyleGroup, List<TextTransform>>> =
        StyleGroup.entries
            .map { group -> group to all.filter { it.group == group } }
            .filter { (_, styles) -> styles.isNotEmpty() }

    private val byIdIndex: Map<String, TextTransform> by lazy { all.associateBy { it.id } }

    fun byId(id: String): TextTransform? = byIdIndex[id]

    private val indexById: Map<String, Int> by lazy { all.withIndex().associate { (i, t) -> t.id to i } }

    fun indexOf(transform: TextTransform): Int = indexById[transform.id] ?: -1
}
