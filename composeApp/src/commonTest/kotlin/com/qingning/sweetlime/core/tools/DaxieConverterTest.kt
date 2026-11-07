package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DaxieConverterTest {

    @Test
    fun roundsThirdDecimalInsteadOfTruncating() {

        assertEquals("人民币壹元零壹分", DaxieConverter.toUpper("1.005"), "1.005 应进位成 1.01")

        assertEquals("人民币贰元整", DaxieConverter.toUpper("2.004"), "2.004 应舍去成 2.00")
    }

    @Test
    fun carriesOverToYuan() {

        assertEquals("人民币贰元整", DaxieConverter.toUpper("1.999"), "1.999 应进位成 2.00")
        assertEquals("人民币壹元整", DaxieConverter.toUpper("0.999"), "0.999 应进位成 1.00")
    }

    @Test
    fun keepsWholeNumberBehaviour() {
        assertEquals("人民币壹仟贰佰叁拾肆元伍角", DaxieConverter.toUpper("1234.5"))
        assertEquals("人民币壹元整", DaxieConverter.toUpper("1.00"))
    }

    @Test
    fun keepsZeroYuan() {

        assertEquals("人民币零元整", DaxieConverter.toUpper("0.00"))
        assertEquals("人民币零元整", DaxieConverter.toUpper("0"), "0 是合法金额，不该判非法")
        assertEquals("人民币零元零壹分", DaxieConverter.toUpper("0.01"))
        assertEquals("人民币零元伍角", DaxieConverter.toUpper("0.5"))
    }

    @Test
    fun rejectsGarbage() {
        assertNull(DaxieConverter.toUpper(""))
        assertNull(DaxieConverter.toUpper("abc"))
        assertNull(DaxieConverter.toUpper("1.2.3"))
    }
}