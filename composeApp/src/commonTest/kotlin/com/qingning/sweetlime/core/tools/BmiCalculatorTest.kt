package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BmiCalculatorTest {

    @Test
    fun normals() {
        val report = BmiCalculator.compute(170.0, 65.0)
        assertEquals(BmiLevel.NORMAL, report?.level)
        assertEquals("22.5", report?.bmiText)
    }

    @Test
    fun levelsFollowChineseStandard() {
        assertEquals(BmiLevel.THIN, BmiCalculator.compute(160.0, 45.0)?.level)
        assertEquals(BmiLevel.NORMAL, BmiCalculator.compute(170.0, 65.0)?.level)
        assertEquals(BmiLevel.OVERWEIGHT, BmiCalculator.compute(170.0, 70.0)?.level)
        assertEquals(BmiLevel.OBESE, BmiCalculator.compute(170.0, 82.0)?.level)
    }

    @Test
    fun rejectsAbsurdInput() {
        assertNull(BmiCalculator.compute(30.0, 10.0))
        assertNull(BmiCalculator.compute(170.0, 0.0))
        assertNull(BmiCalculator.compute(170.0, 600.0))
        assertNull(BmiCalculator.compute(Double.NaN, 60.0))
    }

    @Test
    fun parsesUnits() {
        assertEquals(170.0, BmiCalculator.parseNumber("170cm"))
        assertEquals(65.0, BmiCalculator.parseNumber("65 kg"))
        assertEquals(170.0, BmiCalculator.parseNumber(" 170 " ))
        assertNull(BmiCalculator.parseNumber("abc"))
    }

    @Test
    fun formatsOneDecimal() {
        assertEquals("18.0", BmiCalculator.format1(18.0))
        assertEquals("22.5", BmiCalculator.format1(22.4913))
    }
}
