package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.forEachCodePoint

/**
 * 给文字加「前缀 + 字间符号 + 后缀」。
 *
 * 规则和内置的「点间隔」一致：
 * - 首字前面不加 [between]，末字后面也不加；
 * - **换行处不插**（换行前、换行后都不插），否则多行文本会变成一堆符号；
 * - [input] 为空时直接返回空串（只填了三段装饰也不该凭空产出东西）。
 *
 * 按**码点**遍历：emoji 是代理对，按 UTF-16 单元插会把符号插进两个代理中间。
 */
internal fun decorate(input: String, prefix: String, between: String, suffix: String): String {
    if (input.isEmpty()) return ""
    val sb = StringBuilder(input.length * 2 + prefix.length + suffix.length)
    sb.append(prefix)
    var prev: String? = null
    input.forEachCodePoint { cp ->
        val isBreak = cp == "\n" || cp == "\r"
        val prevIsBreak = prev == null || prev == "\n" || prev == "\r"
        if (!prevIsBreak && !isBreak) sb.append(between)
        sb.append(cp)
        prev = cp
    }
    sb.append(suffix)
    return sb.toString()
}
