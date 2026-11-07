package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

class QqNameColorTest {

    private val isolate = '\u2067'
    private val lri = '\u2066'

    @Test
    fun colorTagEncodesChannelsAsCodePoints() {
        assertEquals("<&\u00FF\u00FF\u0000\u0000>", QqNameColor.colorTag(255, 255, 0, 0))

        assertEquals("<&\u00FF\u0000\u00FF\u00FF>", QqNameColor.colorTag(255, 0, 255, 255))
    }

    @Test
    fun channelsAreClamped() {

        assertEquals("<&\u00FF\u0000\u0000\u0000>", QqNameColor.colorTag(300, -5, 0, 0))
    }

    @Test
    fun splitsTrailingPunctuation() {
        assertEquals("~" to "喵", QqNameColor.splitTrailingPuncts("喵~"))
        assertEquals("~！" to "喵", QqNameColor.splitTrailingPuncts("喵~！"))
        assertEquals("" to "喵", QqNameColor.splitTrailingPuncts("喵"))
        assertEquals("" to "~喵", QqNameColor.splitTrailingPuncts("~喵"))
        assertEquals("" to "", QqNameColor.splitTrailingPuncts(""))
    }

    @Test
    fun buildsFullString() {

        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + "~喵" + lri,
            QqNameColor.build("洛铭酱", "喵~"),
        )
    }

    @Test
    fun emptyTailStillKeepsControlChars() {

        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + lri,
            QqNameColor.build("洛铭酱", ""),
        )
    }

    @Test
    fun nicknameAndTailEndsAreTrimmed() {

        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛 铭" + isolate + "喵 喵" + lri,
            QqNameColor.build("  洛 铭  ", "  喵 喵   "),
        )
    }

    @Test
    fun emptyNicknameWithColorGivesJustColorTag() {

        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + isolate + lri,
            QqNameColor.build("", ""),
        )
    }

    @Test
    fun customColorGoesIntoTag() {

        assertEquals(
            "<&\u00FF\u0000\u00FF\u00FF>" + "洛" + isolate + lri,
            QqNameColor.build("洛", "", 255, 0, 255, 255),
        )
    }

    @Test
    fun colorOnlyEqualsUpstreamEmptyTail() {
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + lri,
            QqNameColor.build("洛铭酱", "喵~", useTail = false),
        )
    }

    @Test
    fun tailOnlyDropsColorTag() {

        assertEquals(
            "洛铭酱" + isolate + "~喵" + lri,
            QqNameColor.build("洛铭酱", "喵~", useColor = false),
        )
    }

    @Test
    fun neitherSwitchMeansPlainNickname() {

        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "喵~", useColor = false, useTail = false))

        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "", useColor = false))
    }

    @Test
    fun tailSwitchOnWithBlankTailFallsBackToPlainNicknameWhenNoColor() {
        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "   ", useColor = false))
    }
}