package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.httpGetText

/** 一份汇率表：1 [base] = rates[X] 个 X。 */
class CurrencyRates(
    val base: String,
    val updatedUtc: String,
    val rates: Map<String, Double>,
) {
    /** 1 个 [from] 等于多少 [to]；缺数据返回 null。 */
    fun crossRate(from: String, to: String): Double? {
        if (from == to) return 1.0
        val f = rates[from] ?: return null
        val t = rates[to] ?: return null
        if (f <= 0.0) return null
        return t / f
    }

    fun convert(amount: Double, from: String, to: String): Double? =
        crossRate(from, to)?.let { amount * it }
}

/**
 * 汇率数据源：exchangerate-api 的免费接口（无需 key）。
 *
 * 只拉一次「以 CNY 为基准」的全量汇率，之后任意两种货币之间都用交叉汇率本地算 ——
 * 换货币不再触发网络请求，省流量，也不会因为连点两下就闪。
 */
object CurrencyApi {

    private const val ENDPOINT = "https://open.er-api.com/v6/latest/"

    /** 常见货币（中文名 → 代码），也是下拉里的展示顺序。 */
    val COMMON: List<Pair<String, String>>
    get() = listOf(
        tr("人民币") to "CNY",
        tr("美元") to "USD",
        tr("欧元") to "EUR",
        tr("英镑") to "GBP",
        tr("日元") to "JPY",
        tr("港币") to "HKD",
        tr("韩元") to "KRW",
        tr("新台币") to "TWD",
        tr("新加坡元") to "SGD",
        tr("澳元") to "AUD",
        tr("加元") to "CAD",
        tr("泰铢") to "THB",
    )

    fun displayName(code: String): String =
        COMMON.firstOrNull { it.second == code }?.first ?: code

    /** 拉一份以 [base] 为基准的汇率表；网络失败返回 null。 */
    suspend fun fetch(base: String = "CNY"): CurrencyRates? {
        val body = httpGetText(ENDPOINT + base) ?: return null
        return parse(body, base)
    }

    /** 解析接口返回体（抽出来单独放着，也免得 JSON 解析散进 UI 里）。 */
    fun parse(body: String, fallbackBase: String): CurrencyRates? {
        if (!body.contains("\"rates\"")) return null
        val table = LinkedHashMap<String, Double>()
        for (match in RATE_ENTRY.findAll(body)) {
            val value = match.groupValues[2].toDoubleOrNull() ?: continue
            table[match.groupValues[1].uppercase()] = value
        }
        if (table.isEmpty()) return null
        val base = BASE_ENTRY.find(body)?.groupValues?.get(1)?.uppercase() ?: fallbackBase
        val updated = UPDATED_ENTRY.find(body)?.groupValues?.get(1) ?: ""
        return CurrencyRates(base, updated, table)
    }

    /** `"USD":0.1428` 这样的键值对。 */
    private val RATE_ENTRY =
        Regex("\"([A-Za-z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?)")

    private val BASE_ENTRY = Regex("\"base_code\"\\s*:\\s*\"([A-Za-z]{3})\"")

    private val UPDATED_ENTRY = Regex("\"time_last_update_utc\"\\s*:\\s*\"([^\"]*)\"")
}