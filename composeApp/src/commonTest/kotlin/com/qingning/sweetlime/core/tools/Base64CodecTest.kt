package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class Base64CodecTest {

    @Test
    fun encodesAscii() {
        assertEquals("aGVsbG8=", Base64Codec.encode("hello"))
        assertEquals("", Base64Codec.encode(""))
    }

    @Test
    fun roundTripsChinese() {
        val text = "你好，世界"
        assertEquals(text, Base64Codec.decode(Base64Codec.encode(text)))
    }

    @Test
    fun ignoresWhitespaceAndMissingPadding() {
        assertEquals("hello", Base64Codec.decode("aGVs\nbG8="))
        assertEquals("hello", Base64Codec.decode("aGVs bG8="))
        assertEquals("hello", Base64Codec.decode("aGVsbG8"))
    }

    @Test
    fun acceptsUrlSafeAlphabet() {
        // -和 _是 url-safe变体，应当与 +/得到同一个结果。
        assertEquals(Base64Codec.decode("+/+/"), Base64Codec.decode("-_-_"))
    }
}
