
package top.yukonga.miuix.kmp.glass.internal

import kotlin.math.sqrt

internal const val TILE_SCALE = 1.5286649465560913f

internal const val FIT_4 = -0.7391197269f
internal const val FIT_3 = 2.4034927648f
internal const val FIT_2 = -2.4907319173f
internal const val FIT_1 = 0.4768708960f
internal const val FIT_0 = 0.4747847594f

internal const val MIN_SUPERCIRCLE_RADIUS = 0.5f

internal fun smooth5Map(t: Float): Float {
    val x = (0.5f + 0.5f * t).coerceIn(0f, 1f)
    val s = x * x * x * (x * (x * 6f - 15f) + 10f)
    return (s - 0.5f) * 2f
}

internal fun coverageThreshold(band: Float, edgeFullPx: Float): Float {
    val ratio = (band / if (edgeFullPx > 1f) edgeFullPx else 1f).coerceIn(0f, 1f)
    return smooth5Map(sqrt(ratio))
}
