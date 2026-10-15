package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform
import com.qingning.sweetlime.core.forEachCodePoint

/**
 * 给每个可见字符后面追加一个“组合字符”（combining mark），
 * 浏览器 / 系统渲染时就会把它画在字符上方或下方。
 *
 * 注意：不同设备、不同 App 的字体对组合字符的支持差异很大，
 * 中文场景可能出现错位或方框，这属于渲染差异而非转换错误。
 */
private fun combineEach(input: String, mark: Char): String =
    buildString(input.length * 2) {
        // 按码点走：组合符要加在**整个字符**之后，加在代理对中间会毁掉 emoji。
        input.forEachCodePoint { char ->
            val isWhitespace = char.length == 1 && char[0].isWhitespace()
            if (char == "\n" || char == "\r" || isWhitespace) {
                append(char)
            } else {
                append(char)
                append(mark)
            }
        }
    }

private fun overlayStyle(id: String, title: String, mark: Char): TextTransform =
    transform(id, title, StyleGroup.OVERLAY) { input -> combineEach(input, mark) }

internal val overlayStyles: List<TextTransform>
    get() = listOf(
    overlayStyle("overlay_underline", "下划线", '\u0332'),      // COMBINING LOW LINE
    overlayStyle("overlay_strike", "删除线", '\u0336'),         // COMBINING LONG STROKE OVERLAY
    overlayStyle("overlay_slash", "斜杠", '\u0338'),            // COMBINING LONG SOLIDUS OVERLAY
    overlayStyle("overlay_dot_above", "上点", '\u0307'),        // COMBINING DOT ABOVE
    overlayStyle("overlay_tilde_below", "波浪线", '\u0330'),    // COMBINING TILDE BELOW
)