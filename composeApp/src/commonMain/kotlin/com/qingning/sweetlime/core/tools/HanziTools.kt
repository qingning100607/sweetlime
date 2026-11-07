package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.mapping.CHAI_MAP
import com.qingning.sweetlime.core.mapping.PINYIN_MAP
import com.qingning.sweetlime.core.forEachCodePoint

internal fun hanziToPinyin(input: String): String {
    if (input.isEmpty()) return ""
    val sb = StringBuilder(input.length * 2)
    input.forEach { ch ->
        val pinyin = PINYIN_MAP[ch]
        if (pinyin != null) sb.append(pinyin).append(' ') else sb.append(ch)
    }
    return sb.toString().trimEnd()
}

internal fun hanziToChai(input: String): String {
    if (input.isEmpty()) return ""
    val sb = StringBuilder(input.length * 2)
    input.forEach { ch -> sb.append(CHAI_MAP[ch] ?: ch.toString()) }
    return sb.toString()
}

internal fun toFlyingBird(input: String): String = buildString(input.length * 2) {

    input.forEachCodePoint { ch ->
        append(ch)
        if (ch != "\n" && ch != "\r") append('\u0F7C')
    }
}