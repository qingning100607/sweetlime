package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.mapping.CHAI_MAP
import com.qingning.sweetlime.core.mapping.PINYIN_MAP

/**
 * 汉字 → 带声调拼音。字典里查不到的字（标点、英文、数字）原样保留，
 * 每个成功转换的汉字后面补一个空格，与原站输出一致。
 */
internal fun hanziToPinyin(input: String): String {
    if (input.isEmpty()) return ""
    val sb = StringBuilder(input.length * 2)
    input.forEach { ch ->
        val pinyin = PINYIN_MAP[ch]
        if (pinyin != null) sb.append(pinyin).append(' ') else sb.append(ch)
    }
    return sb.toString().trimEnd()
}

/**
 * 汉字拆分：把左右结构的字拆成部件（如「卧」→「臣卜」）。
 * 表里没有的字原样保留，与原站 `ret += zk[c] ? zk[c] : c` 的行为一致。
 */
internal fun hanziToChai(input: String): String {
    if (input.isEmpty()) return ""
    val sb = StringBuilder(input.length * 2)
    input.forEach { ch -> sb.append(CHAI_MAP[ch] ?: ch.toString()) }
    return sb.toString()
}

/** 飞鸟文：每个非换行字符后面追加组合字符 ོ（U+0F7C）。 */
internal fun toFlyingBird(input: String): String = buildString(input.length * 2) {
    input.forEach { ch ->
        append(ch)
        if (ch != '\n' && ch != '\r') append('\u0F7C')
    }
}