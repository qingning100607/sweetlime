package com.qingning.sweetlime.core.tools

object CssGradient {

    data class Stop(val color: String, val position: Int)

    fun linear(angle: Int, start: String, end: String): String {
        val a = start.trim().ifEmpty { "#FF5722" }
        val b = end.trim().ifEmpty { "#2196F3" }
        return "background: linear-gradient(${angle}deg, $a 0%, $b 100%);"
    }

    fun radial(start: String, end: String): String {
        val a = start.trim().ifEmpty { "#FF5722" }
        val b = end.trim().ifEmpty { "#2196F3" }
        return "background: radial-gradient(circle, $a 0%, $b 100%);"
    }

    fun multi(angle: Int, colors: List<String>): String {
        if (colors.isEmpty()) return linear(angle, "#FF5722", "#2196F3")
        val stops = colors.mapIndexed { index, color ->
            val pos = if (colors.size == 1) 0 else index * 100 / (colors.lastIndex)
            "${color.trim()} $pos%"
        }
        return "background: linear-gradient(${angle}deg, ${stops.joinToString(", ")});"
    }
}