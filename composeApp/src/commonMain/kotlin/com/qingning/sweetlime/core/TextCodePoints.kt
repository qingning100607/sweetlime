package com.qingning.sweetlime.core

internal fun String.codePointLength(): Int {
    var n = 0
    var i = 0
    while (i < length) {
        i += if (this[i].isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) 2 else 1
        n++
    }
    return n
}

internal inline fun String.forEachCodePoint(action: (String) -> Unit) {
    var i = 0
    while (i < length) {
        val ch = this[i]
        if (ch.isHighSurrogate() && i + 1 < length && this[i + 1].isLowSurrogate()) {
            action(substring(i, i + 2))
            i += 2
        } else {
            action(ch.toString())
            i++
        }
    }
}

internal fun String.listPreview(max: Int = 100): String {
    val flat = replace('\n', ' ')
    var n = 0
    var i = 0
    while (i < flat.length) {
        if (n == max) return flat.substring(0, i) + "…"
        i += if (flat[i].isHighSurrogate() && i + 1 < flat.length && flat[i + 1].isLowSurrogate()) 2 else 1
        n++
    }
    return flat
}
