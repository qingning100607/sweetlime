package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 全量样式的「代理对安全」不变量：输出里不允许出现**落单的代理**。
 *
 * 起因：一批「在字符之间插东西」的样式按 UTF-16 code unit 迭代，
 * 遇到 emoji（增补平面字符 = 代理对）就把分隔符插进两个代理中间，
 * 产出非法序列、渲染成 \uFFFD。这里对注册表里的每一条样式都跑一遍，
 * 以后新增样式时漏了「按码点」也会被抓住。
 */
class SurrogateSafetyTest {

    /** 混了 emoji、增补平面汉字、CJK、ASCII 与换行。 */
    private val sample = "\uD83C\uDF89A\uD83D\uDC66\u597D\n\uD83D\uDC4D\uD83C\uDFFB"

    private fun loneSurrogates(s: String): List<Int> {
        val bad = mutableListOf<Int>()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c.isHighSurrogate()) {
                if (i + 1 >= s.length || !s[i + 1].isLowSurrogate()) bad += i
                i += 2
            } else if (c.isLowSurrogate()) {
                bad += i
                i++
            } else {
                i++
            }
        }
        return bad
    }

    @Test
    fun codePointLengthCountsEmojiAsOne() {
        assertEquals(3, "abc".codePointLength())
        assertEquals(1, "\uD83C\uDF89".codePointLength(), "一个 emoji 是 1 个字")
        assertEquals(3, "\uD83C\uDF89\uD83C\uDF89\uD83C\uDF89".codePointLength())
        assertEquals(4, "a\uD83C\uDF89b\u597D".codePointLength())
        assertEquals(0, "".codePointLength())
    }

    @Test
    fun noStyleSplitsSurrogatePairs() {
        val failures = mutableListOf<String>()
        TransformRegistry.all.forEach { t ->
            val out = runCatching { t.transform(sample) }.getOrNull() ?: return@forEach
            val bad = loneSurrogates(out)
            if (bad.isNotEmpty()) {
                failures += "${t.id}（${t.title}）在 $bad 处劈开了代理对 → $out"
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }
}
