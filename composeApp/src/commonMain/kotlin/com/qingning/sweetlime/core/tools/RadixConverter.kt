package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.i18n.trf

object RadixConverter {

    const val MIN_BASE = 2
    const val MAX_BASE = 36

    private const val DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz"

    fun convert(value: String, fromBase: Int, toBase: Int): String? {
        if (fromBase !in MIN_BASE..MAX_BASE || toBase !in MIN_BASE..MAX_BASE) return null
        var s = value.filterNot { it.isWhitespace() || it == '_' }
        if (s.isEmpty()) return null

        var negative = false
        when (s.first()) {
            '-' -> {
                negative = true
                s = s.drop(1)
            }
            '+' -> s = s.drop(1)
        }
        s = when {
            fromBase == 16 && s.startsWith("0x", ignoreCase = true) -> s.drop(2)
            fromBase == 8 && s.startsWith("0o", ignoreCase = true) -> s.drop(2)
            fromBase == 2 && s.startsWith("0b", ignoreCase = true) -> s.drop(2)
            else -> s
        }
        if (s.isEmpty()) return null

        val digits = IntArray(s.length)
        for ((index, ch) in s.withIndex()) {
            val v = DIGITS.indexOf(ch.lowercaseChar())
            if (v < 0 || v >= fromBase) return null
            digits[index] = v
        }

        val out = StringBuilder()
        val work = digits.copyOf()
        while (true) {
            var remainder = 0
            var allZero = true
            for (i in work.indices) {
                val acc = remainder * fromBase + work[i]
                work[i] = acc / toBase
                remainder = acc % toBase
                if (work[i] != 0) allZero = false
            }
            out.append(DIGITS[remainder])
            if (allZero) break
        }
        val body = out.reverse().toString().trimStart('0').ifEmpty { "0" }
        return if (negative && body != "0") "-$body" else body
    }

    fun convertToCommon(value: String, fromBase: Int): List<Pair<String, String>>? {
        val result = ArrayList<Pair<String, String>>(5)
        for (base in listOf(2, 8, 10, 16)) {
            val out = convert(value, fromBase, base) ?: return null
            result.add(baseLabel(base) to out)
        }
        val hex = convert(value, fromBase, 16) ?: return null
        result.add(tr("十六进制（大写）") to hex.uppercase())
        return result
    }

    fun baseLabel(base: Int): String = when (base) {
        2 -> tr("二进制")
        8 -> tr("八进制")
        10 -> tr("十进制")
        16 -> tr("十六进制")
        else -> trf("{} 进制", base)
    }
}