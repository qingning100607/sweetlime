package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 钉住「美化昵称串」的生成规则。
 *
 * 这套串里除了昵称全是**不可见字符**，肉眼根本看不出顺序错没错，但字符顺序错了
 * QQ 那边就不认（不上色 / 尾巴错位），所以必须靠测试守着 —— 规则来源是上游网页
 * `qq-name-beautify` 的 `_doCopy()`。
 */
class QqNameColorTest {

    private val isolate = '\u2067'
    private val lri = '\u2066'

    @Test
    fun colorTagEncodesChannelsAsCodePoints() {
        // 上游默认值：不透明红 → `<&` + chr(255) + chr(255) + chr(0) + chr(0) + `>`
        assertEquals("<&\u00FF\u00FF\u0000\u0000>", QqNameColor.colorTag(255, 255, 0, 0))
        // 换个颜色：不透明青（0,255,255）
        assertEquals("<&\u00FF\u0000\u00FF\u00FF>", QqNameColor.colorTag(255, 0, 255, 255))
    }

    @Test
    fun channelsAreClamped() {
        // 超范围的通道值会被夹到 0..255，而不是抛异常或溢出成别的字符。
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
        // 尾巴 `喵~`：结尾的 `~` 被切下来倒序后拼到最前 → `~喵`
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + "~喵" + lri,
            QqNameColor.build("洛铭酱", "喵~"),
        )
    }

    @Test
    fun emptyTailStillKeepsControlChars() {
        // 尾巴留空时，三个控制字符依然要在：少了它们 QQ 那边的整串方向会乱。
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + lri,
            QqNameColor.build("洛铭酱", ""),
        )
    }

    @Test
    fun nicknameAndTailEndsAreTrimmed() {
        // 昵称和尾巴的首尾空白都去掉：尾巴前面多一个空格会在名字和尾巴之间留空档。
        // 中间的空格照旧保留（那是用户自己排的版）。
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛 铭" + isolate + "喵 喵" + lri,
            QqNameColor.build("  洛 铭  ", "  喵 喵   "),
        )
    }


    @Test
    fun emptyNicknameWithColorGivesJustColorTag() {
        // 昵称留空、只开颜色：结果就是「颜色标记 + 三个控制字符」，
        // 用户把它粘到群昵称最前面，就给自己现在的昵称上色（不用重打名字）。
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + isolate + lri,
            QqNameColor.build("", ""),
        )
    }

    @Test
    fun customColorGoesIntoTag() {
        // 绿=255、蓝=255、不透明：青
        assertEquals(
            "<&\u00FF\u0000\u00FF\u00FF>" + "洛" + isolate + lri,
            QqNameColor.build("洛", "", 255, 0, 255, 255),
        )
    }

    /* ---------------- 两个开关（颜色 / 尾巴）的四种组合 ---------------- */

    @Test
    fun colorOnlyEqualsUpstreamEmptyTail() {
        // 只开颜色：控制字符照样要在（和上游把尾巴留空时的产出一样）。
        assertEquals(
            "<&\u00FF\u00FF\u0000\u0000>" + "洛铭酱" + isolate + lri,
            QqNameColor.build("洛铭酱", "喵~", useTail = false),
        )
    }

    @Test
    fun tailOnlyDropsColorTag() {
        // 只开尾巴：颜色标记没了，尾巴的标点倒序照旧。
        assertEquals(
            "洛铭酱" + isolate + "~喵" + lri,
            QqNameColor.build("洛铭酱", "喵~", useColor = false),
        )
    }

    @Test
    fun neitherSwitchMeansPlainNickname() {
        // 两个都关：给的就是昵称本身，一个不可见字符都不掺。
        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "喵~", useColor = false, useTail = false))
        // 没开颜色、尾巴又是空的：同样给裸昵称。
        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "", useColor = false))
    }

    @Test
    fun tailSwitchOnWithBlankTailFallsBackToPlainNicknameWhenNoColor() {
        assertEquals("洛铭酱", QqNameColor.build("洛铭酱", "   ", useColor = false))
    }
}