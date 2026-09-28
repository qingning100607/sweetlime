package com.qingning.sweetlime.core.tools

/**
 * 进制转换（2–36 进制，任意长度）。
 *
 * 没有用 `Long.parseLong`：那玩意最多 64 位，粘贴一个长一点的二进制串就溢出了。
 * 这里用「大整数按位数组做短除法」的方式，位数多少都不怕，纯字符串进出。
 */
object RadixConverter {

    const val MIN_BASE = 2
    const val MAX_BASE = 36

    private const val DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz"

    /**
     * 把 [value] 从 [fromBase] 转成 [toBase]。不合法返回 null。
     *
     * 容错：忽略空格 / 下划线 / 换行；接受前导 `+` / `-`；
     * 进制为 16 / 8 / 2 时也认 `0x`、`0o`、`0b` 前缀。
     */
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

        // 先解析成「每位的数值」数组，顺便校验每一位都落在进制范围内。
        val digits = IntArray(s.length)
        for ((index, ch) in s.withIndex()) {
            val v = DIGITS.indexOf(ch.lowercaseChar())
            if (v < 0 || v >= fromBase) return null
            digits[index] = v
        }

        // 反复「除 toBase 取余」：每轮把整个位数组当一个大整数做一次除法。
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

    /** 常用进制的一次性输出（二进制 / 八进制 / 十进制 / 十六进制 + 大写十六进制）。 */
    fun convertToCommon(value: String, fromBase: Int): List<Pair<String, String>>? {
        val result = ArrayList<Pair<String, String>>(5)
        for (base in listOf(2, 8, 10, 16)) {
            val out = convert(value, fromBase, base) ?: return null
            result.add(baseLabel(base) to out)
        }
        val hex = convert(value, fromBase, 16) ?: return null
        result.add("十六进制（大写）" to hex.uppercase())
        return result
    }

    /** 下拉里显示的名字。 */
    fun baseLabel(base: Int): String = when (base) {
        2 -> "二进制"
        8 -> "八进制"
        10 -> "十进制"
        16 -> "十六进制"
        else -> "$base 进制"
    }
}