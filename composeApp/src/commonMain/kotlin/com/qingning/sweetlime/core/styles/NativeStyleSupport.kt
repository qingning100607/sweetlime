package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.NativeText
import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform

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

internal val kotlinStyleRefs: MutableMap<String, (String) -> String> = mutableMapOf()
