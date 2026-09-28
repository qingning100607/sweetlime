package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ColorConverterTest {

    @Test
    fun expandsShortHex() {
        val color = ColorConverter.parse("#f00")
        assertNotNull(color)
        assertEquals(255, color.red)
        assertEquals(0, color.green)
        assertEquals(0, color.blue)
        assertEquals(255, color.alpha)
        assertEquals("#FF0000", color.hex)
    }

    @Test
    fun eightDigitsAreAarrggbb() {
        val color = ColorConverter.parse("#80FF0000")
        assertNotNull(color)
        assertEquals(128, color.alpha)
        assertEquals(255, color.red)
        assertEquals("#80FF0000", color.hexAlpha)
    }

    @Test
    fun parsesRgbAndHsl() {
        val rgb = ColorConverter.parse("rgb(1, 2, 3)")
        assertNotNull(rgb)
        assertEquals(1, rgb.red)
        assertEquals(3, rgb.blue)

        val hsl = ColorConverter.parse("hsl(0, 100%, 50%)")
        assertNotNull(hsl)
        assertEquals(255, hsl.red)
        assertEquals(0, hsl.green)
        assertEquals(0, hsl.blue)
    }

    @Test
    fun rejectsGarbage() {
        assertNull(ColorConverter.parse(""))
        assertNull(ColorConverter.parse("nope"))
        assertNull(ColorConverter.parse("#12345"))
    }
}
