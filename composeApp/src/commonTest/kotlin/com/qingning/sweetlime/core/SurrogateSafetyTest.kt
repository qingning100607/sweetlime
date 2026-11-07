package com.qingning.sweetlime.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SurrogateSafetyTest {

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
