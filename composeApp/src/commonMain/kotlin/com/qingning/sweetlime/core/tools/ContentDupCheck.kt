package com.qingning.sweetlime.core.tools

object ContentDupCheck {

    data class Result(
        val similarityPercent: Int,
        val commonChars: Int,
        val baseChars: Int,
        val targetChars: Int,
    )

    private const val MAX_LEN = 2000

    fun normalize(text: String): String {
        val sb = StringBuilder(text.length)
        val it = text.iterator()
        while (it.hasNext()) {
            val ch = it.next()
            if (ch.isLetterOrDigit()) sb.append(ch.lowercaseChar())
        }
        return sb.toString()
    }

    fun compare(base: String, target: String): Result {
        val a = normalize(base).take(MAX_LEN)
        val b = normalize(target).take(MAX_LEN)
        val common = lcsLength(a, b)
        val total = a.length + b.length
        val percent = if (total == 0) 0 else (common * 200 + total) / (total * 2)
        return Result(
            similarityPercent = percent.coerceIn(0, 100),
            commonChars = common,
            baseChars = a.length,
            targetChars = b.length,
        )
    }

    private fun lcsLength(a: String, b: String): Int {
        if (a.isEmpty() || b.isEmpty()) return 0
        val prev = IntArray(b.length + 1)
        val cur = IntArray(b.length + 1)
        for (i in 1..a.length) {
            val ca = a[i - 1]
            for (j in 1..b.length) {
                cur[j] = if (ca == b[j - 1]) prev[j - 1] + 1 else maxOf(prev[j], cur[j - 1])
            }
            prev.indices.forEach { prev[it] = cur[it] }
        }
        return prev[b.length]
    }
}
