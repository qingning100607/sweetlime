package com.qingning.sweetlime.core.tools

import kotlin.math.roundToInt

/**
 * 一个颜色，以及它在各种写法下的表示。
 *
 * 放在最外层（而不是塞进 [ColorConverter] 里面）是为了让 HSL / HSV 的换算直接写在类里 ——
 * 嵌套类访问外部 object 的成员会绕一圈，不如各管各的清楚。
 */
class ColorInfo(
    val alpha: Int,
    val red: Int,
    val green: Int,
    val blue: Int,
) {
    /** `#RRGGBB`。 */
    val hex: String get() = "#${part(red)}${part(green)}${part(blue)}"

    /** 带透明度的 `#AARRGGBB`。 */
    val hexAlpha: String get() = "#${part(alpha)}${part(red)}${part(green)}${part(blue)}"

    val rgb: String get() = "rgb($red, $green, $blue)"

    val rgba: String get() = "rgba($red, $green, $blue, $alphaPercent%)"

    /** `0xAARRGGBB`，Android / Compose 里最常用的写法。 */
    val argb: String get() = "0x${part(alpha)}${part(red)}${part(green)}${part(blue)}".uppercase()

    val hsl: String get() {
        val (h, s, l) = rgbToHsl()
        return "hsl(${h.roundToInt()}, ${(s * 100).roundToInt()}%, ${(l * 100).roundToInt()}%)"
    }

    val hsv: String get() {
        val (h, s, v) = rgbToHsv()
        return "hsv(${h.roundToInt()}, ${(s * 100).roundToInt()}%, ${(v * 100).roundToInt()}%)"
    }

    /** 透明度百分比，0–100。 */
    val alphaPercent: Int get() = (alpha * 100f / 255f).roundToInt()

    /** 给 Compose `Color(...)` 用的一串数字。 */
    val argbLong: Long
        get() = (alpha.toLong() shl 24) or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong()

    private fun part(value: Int): String = value.toString(16).padStart(2, '0').uppercase()

    private fun rgbToHsl(): Triple<Float, Float, Float> {
        val rf = red / 255f
        val gf = green / 255f
        val bf = blue / 255f
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val l = (max + min) / 2f
        val d = max - min
        if (d == 0f) return Triple(0f, 0f, l)
        val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
        val h = when (max) {
            rf -> 60f * (((gf - bf) / d) % 6f)
            gf -> 60f * (((bf - rf) / d) + 2f)
            else -> 60f * (((rf - gf) / d) + 4f)
        }
        return Triple(if (h < 0f) h + 360f else h, s, l)
    }

    private fun rgbToHsv(): Triple<Float, Float, Float> {
        val rf = red / 255f
        val gf = green / 255f
        val bf = blue / 255f
        val max = maxOf(rf, gf, bf)
        val d = max - minOf(rf, gf, bf)
        val s = if (max == 0f) 0f else d / max
        val h = when {
            d == 0f -> 0f
            max == rf -> 60f * (((gf - bf) / d) % 6f)
            max == gf -> 60f * (((bf - rf) / d) + 2f)
            else -> 60f * (((rf - gf) / d) + 4f)
        }
        return Triple(if (h < 0f) h + 360f else h, s, max)
    }
}

/**
 * 颜色代码转换。
 *
 * 输入随便给：`#FF5722`、`FF5722`、`0xFF5722`、`#80FF5722`（带透明度）、
 * `rgb(255, 87, 34)`、`rgba(255,87,34,0.5)`、`hsl(14, 100%, 56%)` 都能认。
 * 输出一次性给全：HEX / RGB / RGBA / HSL / HSV / `0xAARRGGBB`。
 *
 * 八位十六进制按 **AARRGGBB** 解析（Android / Compose 的习惯，透明度在前）。
 */
object ColorConverter {

    private const val NO_MATCH = "\u0000"

    /** 解析任意写法的颜色；认不出来返回 null。 */
    fun parse(text: String): ColorInfo? {
        val s = text.trim().lowercase()
        if (s.isEmpty()) return null

        if (s.startsWith("rgb")) return parseRgb(s)
        if (s.startsWith("hsl")) return parseHsl(s)

        val hex = s.removePrefix("#").removePrefix("0x")
        if (hex.any { it !in "0123456789abcdef" }) return null
        return when (hex.length) {
            3 -> ColorInfo(
                alpha = 255,
                red = hex[0].hexDigit() * 17,
                green = hex[1].hexDigit() * 17,
                blue = hex[2].hexDigit() * 17,
            )
            6 -> ColorInfo(
                alpha = 255,
                red = hex.substring(0, 2).toInt(16),
                green = hex.substring(2, 4).toInt(16),
                blue = hex.substring(4, 6).toInt(16),
            )
            8 -> ColorInfo(
                alpha = hex.substring(0, 2).toInt(16),
                red = hex.substring(2, 4).toInt(16),
                green = hex.substring(4, 6).toInt(16),
                blue = hex.substring(6, 8).toInt(16),
            )
            else -> null
        }
    }

    private fun Char.hexDigit(): Int = this.toString().toInt(16)

    private fun parseRgb(s: String): ColorInfo? {
        val inside = s.substringAfter('(', NO_MATCH).substringBeforeLast(')', NO_MATCH)
        if (inside == NO_MATCH) return null
        val parts = inside.split(',', '/').map { it.trim() }
        if (parts.size !in 3..4) return null
        val r = parts[0].toFloatOrNull()?.roundToInt() ?: return null
        val g = parts[1].toFloatOrNull()?.roundToInt() ?: return null
        val b = parts[2].toFloatOrNull()?.roundToInt() ?: return null
        if (r !in 0..255 || g !in 0..255 || b !in 0..255) return null
        val a = if (parts.size == 3) 255 else alphaOf(parts[3]) ?: return null
        return ColorInfo(a, r, g, b)
    }

    private fun parseHsl(s: String): ColorInfo? {
        val inside = s.substringAfter('(', NO_MATCH).substringBeforeLast(')', NO_MATCH)
        if (inside == NO_MATCH) return null
        val parts = inside.split(',', '/').map { it.trim() }
        if (parts.size !in 3..4) return null
        val h = parts[0].removeSuffix("deg").toFloatOrNull() ?: return null
        val sat = parts[1].removeSuffix("%").toFloatOrNull() ?: return null
        val light = parts[2].removeSuffix("%").toFloatOrNull() ?: return null
        val (r, g, b) = hslToRgb(h, sat / 100f, light / 100f)
        val a = if (parts.size == 3) 255 else alphaOf(parts[3]) ?: return null
        return ColorInfo(a, r, g, b)
    }

    /** 透明度写法：`0.5`（0–1）或 `50%`（也可写 `50`）。 */
    private fun alphaOf(raw: String): Int? {
        val value = raw.removeSuffix("%").toFloatOrNull() ?: return null
        val scaled = if (raw.endsWith("%") || value > 1f) value * 255f / 100f else value * 255f
        return scaled.roundToInt().coerceIn(0, 255)
    }

    private fun hslToRgb(hDeg: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val h = ((hDeg % 360f) + 360f) % 360f / 360f
        if (s == 0f) {
            val v = (l * 255).roundToInt().coerceIn(0, 255)
            return Triple(v, v, v)
        }
        val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
        val p = 2 * l - q
        fun channel(input: Float): Int {
            var t = input
            when {
                t < 0f -> t += 1f
                t > 1f -> t -= 1f
            }
            val value = when {
                t < 1f / 6f -> p + (q - p) * 6f * t
                t < 1f / 2f -> q
                t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
                else -> p
            }
            return (value * 255).roundToInt().coerceIn(0, 255)
        }
        return Triple(channel(h + 1f / 3f), channel(h), channel(h - 1f / 3f))
    }
}