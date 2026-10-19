package com.qingning.sweetlime.core

/**
 * 按**码点**遍历：代理对（emoji / 增补平面字符）作为一个整体回调。
 *
 * 直接用 `String.forEach` 是按 UTF-16 code unit 走的 —— 字符本身没问题，
 * 但凡是「在字符之间插东西」的样式（点间隔、星间隔、下划线、零宽填充、模板…），
 * 分隔符就会被插进两个代理中间，产出**非法 UTF-16**，渲染成 `�`。
 *
 * 所以：置换型（一个字符换一个字符）可以继续用 forEach；
 * 插入型必须用这个。
 */
/** 按码点数长度：emoji 算 1 个字，而不是 UTF-16 单元数的 2 个。 */
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
