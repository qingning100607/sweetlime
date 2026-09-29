package com.qingning.sweetlime.core.tools

import kotlin.math.roundToLong

/**
 * 人民币金额大小写互转。
 *
 * 算法移植自 qiwangming.com 数字大写页的 `dx.js`（`convertCurrency` / `convertToNumber`），
 * 保持完全离线的纯函数实现，不依赖任何网络。
 */
internal object DaxieConverter {

    private val DIGITS = arrayOf("零", "壹", "贰", "叁", "肆", "伍", "陆", "柒", "捌", "玖")
    private val RADICES = arrayOf("", "拾", "佰", "仟")
    private val BIG_RADICES = arrayOf("", "万", "亿")
    private val DECIMALS = arrayOf("角", "分")

    /** 原站限制：小于 1000 亿元。 */
    private const val MAXIMUM_NUMBER = 99999999999.99

    private val CN_MAP: Map<Char, Int> = mapOf(
        '零' to 0, '壹' to 1, '贰' to 2, '叁' to 3, '肆' to 4,
        '伍' to 5, '陆' to 6, '柒' to 7, '捌' to 8, '玖' to 9,
    )

    private val UNIT_MAP: Map<Char, Long> = mapOf(
        '拾' to 10L, '十' to 10L,
        '佰' to 100L, '百' to 100L,
        '仟' to 1000L, '千' to 1000L,
        '万' to 10000L,
        '亿' to 100000000L,
    )

    /**
     * 小写金额 → 大写金额。输入非法（空串、含非数字、超出上限）时返回 `null`。
     * 例：`1234.5` → `人民币壹仟贰佰叁拾肆元伍角`。
     */
    fun toUpper(input: String): String? {
        var text = input.trim().replace(",", "")
        text = text.trimStart('0')
        if (text.isEmpty()) return null
        if (text.any { it != '.' && !it.isDigit() }) return null
        val value = text.toDoubleOrNull() ?: return null
        if (value < 0 || value > MAXIMUM_NUMBER) return null

        val dot = text.indexOf('.')
        val integralText = if (dot >= 0) text.substring(0, dot) else text
        val fracText = if (dot >= 0) text.substring(dot + 1) else ""

        // 四舍五入到分：看小数点后第 3 位，>= 5 就进一位。
        //
        // 这里绝不能走 Double：1.005 在二进制里其实略小于 1.005，乘 100 得到
        // 100.4999…，roundToLong 之后仍然是 100，照样是错的。所以按字符串取位算整数。
        val frac = fracText.padEnd(3, '0').take(3)
        var cents = frac.substring(0, 2).toLong()
        if (frac[2] >= '5') cents++
        var yuan = integralText.toLongOrNull() ?: 0L
        if (cents >= 100) {
            cents -= 100
            yuan += 1
        }
        val integral = yuan.toString()
        val decimal = if (dot < 0) "" else cents.toString().padStart(2, '0')

        val sb = StringBuilder()
        if ((integral.toLongOrNull() ?: 0L) > 0L) {
            var zeroCount = 0
            for (i in integral.indices) {
                val p = integral.length - i - 1
                val d = integral[i]
                val quotient = p / 4
                val modulus = p % 4
                if (d == '0') {
                    zeroCount++
                } else {
                    if (zeroCount > 0) sb.append(DIGITS[0])
                    zeroCount = 0
                    sb.append(DIGITS[d - '0']).append(RADICES[modulus])
                }
                if (modulus == 0 && zeroCount < 4) {
                    sb.append(BIG_RADICES[quotient])
                    zeroCount = 0
                }
            }
            sb.append("元")
        }

        if (decimal.isNotEmpty()) {
            if (decimal == "00") {
                sb.append('整')
            } else {
                val firstZero = decimal[0] == '0'
                val secondZero = decimal.getOrNull(1)?.let { it == '0' } ?: true
                for (i in decimal.indices) {
                    val d = decimal[i]
                    when {
                        d != '0' -> sb.append(DIGITS[d - '0']).append(DECIMALS[i])
                        secondZero -> Unit
                        firstZero -> sb.append('零')
                    }
                }
            }
        }

        var out = sb.toString()
        if (out.isEmpty()) out = "零元"
        if (decimal.isEmpty()) out += "整"
        return "人民币$out"
    }

    /**
     * 大写金额 → 小写金额（保留两位小数）。格式不对时返回 `null`。
     * 例：`人民币壹仟贰佰叁拾肆元伍角` → `1234.50`。
     */
    fun toLower(input: String): String? {
        var text = input.trim()
        if (text.isEmpty()) return null
        if (text.startsWith("人民币")) text = text.removePrefix("人民币")
        if (text.endsWith("整")) text = text.removeSuffix("整")
        val useYuan = text.contains('元')
        val useYuan2 = text.contains('圆')
        if (!useYuan && !useYuan2) return null
        val parts = if (useYuan) text.split('元') else text.split('圆')
        val yuanPart = parts.getOrNull(0).orEmpty()
        val jiaoFenPart = parts.getOrNull(1).orEmpty()

        var total = 0.0
        if (yuanPart.isNotEmpty()) total += parseYuanPart(yuanPart)
        if (jiaoFenPart.isNotEmpty()) total += parseJiaoFenPart(jiaoFenPart)

        val cents = (total * 100).roundToLong()
        val yuan = cents / 100
        val fen = (cents % 100).toString().padStart(2, '0')
        return "$yuan.$fen"
    }

    private fun parseYuanPart(str: String): Double {
        if (str.isEmpty() || str == "零") return 0.0
        var result = 0L
        var currentNum = 0L
        var temp = 0L
        for (ch in str) {
            val digit = CN_MAP[ch]
            if (digit != null) {
                currentNum = digit.toLong()
            } else {
                val unit = UNIT_MAP[ch] ?: continue
                if (unit == 10000L || unit == 100000000L) {
                    temp += currentNum
                    result += temp * unit
                    temp = 0L
                    currentNum = 0L
                } else {
                    temp += currentNum * unit
                    currentNum = 0L
                }
            }
        }
        result += temp + currentNum
        return result.toDouble()
    }

    private fun parseJiaoFenPart(str: String): Double {
        var result = 0.0
        val jiaoIndex = str.indexOf('角')
        if (jiaoIndex > 0) {
            CN_MAP[str[jiaoIndex - 1]]?.let { result += it * 0.1 }
        }
        val fenIndex = str.indexOf('分')
        if (fenIndex > 0) {
            CN_MAP[str[fenIndex - 1]]?.let { result += it * 0.01 }
        }
        return result
    }
}
