package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RadixConverterTest {

    @Test
    fun convertsBetweenBases() {
        assertEquals("ff", RadixConverter.convert("255", 10, 16))
        assertEquals("11111111", RadixConverter.convert("255", 10, 2))
        assertEquals("255", RadixConverter.convert("ff", 16, 10))
    }

    @Test
    fun understandsPrefixesAndSign() {
        assertEquals("255", RadixConverter.convert("0xff", 16, 10))
        assertEquals("8", RadixConverter.convert("0b1000", 2, 10))
        assertEquals("-1010", RadixConverter.convert("-10", 10, 2))
    }

    @Test
    fun doesNotOverflowLong() {
        // 2^96 - 1：超出 Long 的十进制数也得算对。
        assertEquals(
            "ffffffffffffffffffffffff",
            RadixConverter.convert("79228162514264337593543950335", 10, 16),
        )
    }

    @Test
    fun ignoresSeparators() {
        assertEquals("ff", RadixConverter.convert(" 1111_1111 ", 2, 16))
    }

    @Test
    fun rejectsInvalidInput() {
        assertNull(RadixConverter.convert("2", 2, 10))
        assertNull(RadixConverter.convert("", 10, 2))
        assertNull(RadixConverter.convert("10", 1, 10))
        assertNull(RadixConverter.convert("10", 10, 37))
    }

    @Test
    fun commonTableHasUppercaseHex() {
        val rows = RadixConverter.convertToCommon("255", 10)
        assertNotNull(rows)
        assertTrue(rows.any { it.second == "FF" })
        assertEquals("ff", rows.first { it.first == RadixConverter.baseLabel(16) }.second)
    }
}
