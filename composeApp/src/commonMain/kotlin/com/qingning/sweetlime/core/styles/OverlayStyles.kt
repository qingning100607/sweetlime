package com.qingning.sweetlime.core.styles

import com.qingning.sweetlime.core.StyleGroup
import com.qingning.sweetlime.core.TextTransform
import com.qingning.sweetlime.core.transform
import com.qingning.sweetlime.core.forEachCodePoint

private fun combineEach(input: String, mark: Char): String =
    buildString(input.length * 2) {

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
    overlayStyle("overlay_underline", "下划线", '\u0332'),
    overlayStyle("overlay_strike", "删除线", '\u0336'),
    overlayStyle("overlay_slash", "斜杠", '\u0338'),
    overlayStyle("overlay_dot_above", "上点", '\u0307'),
    overlayStyle("overlay_tilde_below", "波浪线", '\u0330'),
)