package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UrlCodecTest {

    @Test
    fun encodesAscii() {
        assertEquals("hello", UrlCodec.encode("hello"))
        assertEquals("", UrlCodec.encode(""))
        // 空格必须是 %20（encodeURIComponent 那套），不是表单的 +
        assertEquals("a%20b", UrlCodec.encode("a b"))
    }

    @Test
    fun encodesChineseAsUtf8() {
        assertEquals("%E4%BD%A0%E5%A5%BD", UrlCodec.encode("你好"))
    }

    @Test
    fun keepsUnreservedCharacters() {
        // -_.!~*'() 与字母数字不转义
        assertEquals("A-z0-9-_.!~*'()", UrlCodec.encode("A-z0-9-_.!~*'()"))
        // 结构字符该转的都要转
        assertEquals("%2F%3F%26%3D%23%2B", UrlCodec.encode("/?&=#+"))
    }

    @Test
    fun roundTrips() {
        for (text in listOf("你好 世界", "a+b/c?d=e&f", "emoji 🙂 也要能过", "100% 确定")) {
            assertEquals(text, UrlCodec.decode(UrlCodec.encode(text)))
        }
    }

    @Test
    fun decodesUpperCaseAndLowerCaseHex() {
        assertEquals("你好", UrlCodec.decode("%e4%bd%a0%e5%a5%bd"))
        assertEquals("你好", UrlCodec.decode("%E4%BD%A0%E5%A5%BD"))
    }

    @Test
    fun treatsPlusAsSpace() {
        assertEquals("a b", UrlCodec.decode("a+b"))
    }

    @Test
    fun rejectsMalformedEscapes() {
        assertNull(UrlCodec.decode("%"))
        assertNull(UrlCodec.decode("%2"))
        assertNull(UrlCodec.decode("%ZZ"))
        assertNull(UrlCodec.decode("abc%4"))
    }

    @Test
    fun leavesPlainTextAlone() {
        assertEquals("hello world", UrlCodec.decode("hello world"))
        assertEquals("", UrlCodec.decode(""))
    }
}