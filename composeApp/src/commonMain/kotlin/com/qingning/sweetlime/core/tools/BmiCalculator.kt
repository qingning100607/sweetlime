package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.i18n.tr
import kotlin.math.roundToInt

/**
 * BMI 的分级（中国成人标准，WS/T428-2013）。
 *
 * 注意和国际口径的差别：这里是 24 算超重的起点、28 算肥胖，
 * 而 WHO 用的是 25 / 30。中文界面下用国内标准更贴合直觉。
 */
enum class BmiLevel(val label: String, val range: String) {
    THIN(tr("偏瘦"), tr("低于 18.5")),
    NORMAL(tr("正常"), "18.5 – 23.9"),
    OVERWEIGHT(tr("超重"), "24.0 – 27.9"),
    OBESE(tr("肥胖"), tr("28.0 及以上")),
}

/** 一次 BMI 计算的全部结果。 */
class BmiReport(
    val bmi: Double,
    val level: BmiLevel,
    val healthyMinKg: Double,
    val healthyMaxKg: Double,
) {
    /** 保留一位小数的 BMI。 */
    val bmiText: String get() = BmiCalculator.format1(bmi)

    /** 例如「正常（18.5 – 23.9）」。 */
    val levelText: String get() = "${level.label}（${level.range}）"

    /** 例如「53.4 – 69.3 kg」。 */
    val healthyRangeText: String
        get() = "${BmiCalculator.format1(healthyMinKg)} – ${BmiCalculator.format1(healthyMaxKg)} kg"

    /** 一句人话。 */
    val advice: String
        get() = when (level) {
            BmiLevel.THIN -> tr("体重偏低，注意补充营养，别盲目节食。")
            BmiLevel.NORMAL -> tr("体重在健康范围，保持规律作息与运动就好。")
            BmiLevel.OVERWEIGHT -> tr("略微超重，适当控制饮食、增加运动量。")
            BmiLevel.OBESE -> tr("已达到肥胖，建议调整饮食结构，必要时咨询专业人士。")
        }
}

/**
 * 身体 BMI 计算：体重(kg) ÷ 身高(m)²。
 *
 * 纯本地公式，不联网、没有依赖表。
 */
object BmiCalculator {

    /** 超出这个范围的输入基本都是打错了，直接判无效而不是硬算。 */
    private const val MIN_HEIGHT_CM = 50.0
    private const val MAX_HEIGHT_CM = 260.0
    private const val MAX_WEIGHT_KG = 500.0

    /** 输入不合法（缺、非数、离谱）时返回 null。 */
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

    /** 保留一位小数。 */
    fun format1(value: Double): String {
        val rounded = (value * 10).roundToInt()
        return "${rounded / 10}.${rounded % 10}"
    }

    /** 解析用户输入的数字：容忍「cm」「kg」后缀、空格和全角空格。 */
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