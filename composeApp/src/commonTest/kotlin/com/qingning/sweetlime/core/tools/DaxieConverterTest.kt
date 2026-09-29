package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 大写金额的边界测试。
 *
 * 重点是「四舍五入到分」：旧实现用 take(2) 把小数点后第三位直接砍掉，
 * 1.005 会变成「壹元整」。金额算错比别处严重，所以这里逐位盯住。
 */
class DaxieConverterTest {

    @Test
    fun roundsThirdDecimalInsteadOfTruncating() {
        // 第三位 >= 5 要进位到分。
        assertEquals("人民币壹元零壹分", DaxieConverter.toUpper("1.005"), "1.005 应进位成 1.01")
        // 第三位 < 5 原样舍去。
        assertEquals("人民币贰元整", DaxieConverter.toUpper("2.004"), "2.004 应舍去成 2.00")
    }

    @Test
    fun carriesOverToYuan() {
        // 分进位到 100 之后要进到元，不能出现「零元」或者「100 分」。
        assertEquals("人民币贰元整", DaxieConverter.toUpper("1.999"), "1.999 应进位成 2.00")
        assertEquals("人民币壹元整", DaxieConverter.toUpper("0.999"), "0.999 应进位成 1.00")
    }

    @Test
    fun keepsWholeNumberBehaviour() {
        assertEquals("人民币壹仟贰佰叁拾肆元伍角", DaxieConverter.toUpper("1234.5"))
        assertEquals("人民币壹元整", DaxieConverter.toUpper("1.00"))
    }

    @Test
    fun rejectsGarbage() {
        assertNull(DaxieConverter.toUpper(""))
        assertNull(DaxieConverter.toUpper("abc"))
        assertNull(DaxieConverter.toUpper("1.2.3"))
    }
}