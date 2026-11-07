package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class ContentDupCheckTest {

    @Test
    fun identicalTextIsHundredPercent() {
        val a = "今天天气很好我们去公园散步吧"
        val r = ContentDupCheck.compare(a, a)
        assertEquals(100, r.similarityPercent)
        assertEquals(a.length, r.commonChars)
    }

    @Test
    fun identicalTextWithPunctuationIsStillHundred() {
        val a = "Hello，世界！Hello, 世界。"
        val r = ContentDupCheck.compare(a, a)
        assertEquals(100, r.similarityPercent)
    }

    @Test
    fun unrelatedTextIsLow() {
        val r = ContentDupCheck.compare("abcdef", "123456")
        assertEquals(0, r.similarityPercent)
    }

    @Test
    fun halfOverlapLooksLikeHalf() {
        val r = ContentDupCheck.compare("abcdef", "abcxyz")
        assertEquals(50, r.similarityPercent)
    }

    @Test
    fun emptyInputIsZero() {
        assertEquals(0, ContentDupCheck.compare("", "").similarityPercent)
        assertEquals(0, ContentDupCheck.compare("只有一边", "").similarityPercent)
    }
}