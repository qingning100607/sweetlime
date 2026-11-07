package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.i18n.tr
import kotlin.math.roundToInt

enum class BmiLevel(private val labelKey: String, private val rangeKey: String) {
    THIN("偏瘦", "低于 18.5"),
    NORMAL("正常", "18.5 – 23.9"),
    OVERWEIGHT("超重", "24.0 – 27.9"),
    OBESE("肥胖", "28.0 及以上"),
    ;

    val label: String get() = tr(labelKey)

    val range: String get() = tr(rangeKey)
}

class BmiReport(
    val bmi: Double,
    val level: BmiLevel,
    val healthyMinKg: Double,
    val healthyMaxKg: Double,
) {

    val bmiText: String get() = BmiCalculator.format1(bmi)

    val levelText: String get() = "${level.label}（${level.range}）"

    val healthyRangeText: String
        get() = "${BmiCalculator.format1(healthyMinKg)} – ${BmiCalculator.format1(healthyMaxKg)} kg"

    val advice: String
        get() = when (level) {
            BmiLevel.THIN -> tr("体重偏低，注意补充营养，别盲目节食。")
            BmiLevel.NORMAL -> tr("体重在健康范围，保持规律作息与运动就好。")
            BmiLevel.OVERWEIGHT -> tr("略微超重，适当控制饮食、增加运动量。")
            BmiLevel.OBESE -> tr("已达到肥胖，建议调整饮食结构，必要时咨询专业人士。")
        }
}

object BmiCalculator {

    private const val MIN_HEIGHT_CM = 50.0
    private const val MAX_HEIGHT_CM = 260.0
    private const val MAX_WEIGHT_KG = 500.0

    fun compute(heightCm: Double, weightKg: Double): BmiReport? {
        if (!heightCm.isFinite() || !weightKg.isFinite()) return null
        if (heightCm !in MIN_HEIGHT_CM..MAX_HEIGHT_CM) return null
        if (weightKg <= 0.0 || weightKg > MAX_WEIGHT_KG) return null

        val meters = heightCm / 100.0
        val bmi = weightKg / (meters * meters)
        val level = when {
            bmi < 18.5 -> BmiLevel.THIN
            bmi < 24.0 -> BmiLevel.NORMAL
            bmi < 28.0 -> BmiLevel.OVERWEIGHT
            else -> BmiLevel.OBESE
        }
        return BmiReport(
            bmi = bmi,
            level = level,
            healthyMinKg = 18.5 * meters * meters,
            healthyMaxKg = 24.0 * meters * meters,
        )
    }

    fun format1(value: Double): String {
        val rounded = (value * 10).roundToInt()
        return "${rounded / 10}.${rounded % 10}"
    }

    fun parseNumber(raw: String): Double? {
        var s = raw.replace("\u3000", "").filterNot { it.isWhitespace() }
        for (suffix in listOf("kg", "KG", "Kg", "kG", "cm", "CM", "Cm", "cM")) {
            if (s.endsWith(suffix)) {
                s = s.removeSuffix(suffix)
                break
            }
        }
        return s.toDoubleOrNull()
    }
}