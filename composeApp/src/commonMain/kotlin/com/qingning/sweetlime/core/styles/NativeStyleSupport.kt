package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.NativeText
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform

/**
 * 走原生快路的样式外壳。
 *
 * 用法和 [transform] 一样，只是**先问一次原生实现**（C++ 那边按 id 认领），
 * 认领了就返回 C++ 的结果，否则用 [block] 这份 Kotlin 实现。
 *
 * [block] 会被登记进 [kotlinStyleRefs]：设备上的自检拿它和原生结果逐字符串对拍，
 * 所以这两条路必须永远等价 —— 改一个地方就要改另一个。
 */
internal fun nativeStyle(
    id: String,
    title: String,
    group: StyleGroup,
    block: (String) -> String,
): TextTransform {
    kotlinStyleRefs[id] = block
    return transform(id, title, group) { input ->
        if (NativeText.available) NativeText.mapStyle(id, input) ?: block(input) else block(input)
    }
}

/** 所有「原生 + Kotlin」双实现的样式的 Kotlin 版，供自检对拍。 */
internal val kotlinStyleRefs: MutableMap<String, (String) -> String> = mutableMapOf()
