package com.qingning.sweetlime.core

import java.nio.charset.Charset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 编码探测与回写。
 *
 * 这是纯 java.nio 的实现，所以放在 Android 侧的单元测试里跑（不需要真机）。
 */
class TextCharsetTest {

    private val chineseBytes = "\u4e2d\u6587\u6d4b\u8bd5".toByteArray(Charset.forName("GB18030"))
    private val chineseText = "\u4e2d\u6587\u6d4b\u8bd5"

    @Test
    fun detectsUtf8() {
        assertEquals("UTF-8", detectTextCharset(chineseText.toByteArray(Charsets.UTF_8)))
    }

    @Test
    fun detectsUtf8WithBom() {
        val withBom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            chineseText.toByteArray(Charsets.UTF_8)
        assertEquals("UTF-8-BOM", detectTextCharset(withBom))
        // 带 BOM 也要能读对内容。
        assertEquals(chineseText, decodeTextBytes(withBom))
    }

    @Test
    fun detectsAndReadsGbk() {
        // GBK 的字节不是合法的 UTF-8，必须靠回退认出来。
        assertEquals("GB18030", detectTextCharset(chineseBytes))
        assertEquals(chineseText, decodeTextBytes(chineseBytes))
    }

    @Test
    fun detectsUtf16() {
        val le = byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + chineseText.toByteArray(Charsets.UTF_16LE)
        assertEquals("UTF-16LE", detectTextCharset(le))
        assertEquals(chineseText, decodeTextBytes(le))

        val be = byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + chineseText.toByteArray(Charsets.UTF_16BE)
        assertEquals("UTF-16BE", detectTextCharset(be))
        assertEquals(chineseText, decodeTextBytes(be))
    }

    @Test
    fun writesBackInTheSameCharset() {
        // 关键：原来是什么编码，存回去还得是什么编码。
        assertTrue(chineseBytes.contentEquals(encodeTextToBytes(chineseText, "GB18030")))
        assertTrue(chineseText.toByteArray(Charsets.UTF_8).contentEquals(
            encodeTextToBytes(chineseText, "UTF-8"),
        ))
    }

    @Test
    fun bomStyleRoundTrips() {
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val written = encodeTextToBytes(chineseText, "UTF-8-BOM")
        assertTrue(written.copyOfRange(0, 3).contentEquals(bom))
        assertEquals(chineseText, decodeTextBytes(written))
    }

    @Test
    fun unknownCharsetFallsBackToUtf8() {
        assertTrue(chineseText.toByteArray(Charsets.UTF_8).contentEquals(
            encodeTextToBytes(chineseText, "\u4e0d\u5b58\u5728\u7684\u7f16\u7801"),
        ))
    }
}