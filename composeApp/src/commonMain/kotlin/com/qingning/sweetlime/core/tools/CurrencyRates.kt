package com.qingning.sweetlime.core.tools

import com.qingning.sweetlime.core.i18n.tr
import com.qingning.sweetlime.core.httpGetText

class CurrencyRates(
    val base: String,
    val updatedUtc: String,
    val rates: Map<String, Double>,
) {

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

object CurrencyApi {

    private const val ENDPOINT = "https://open.er-api.com/v6/latest/"

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

    suspend fun fetch(base: String = "CNY"): CurrencyRates? {
        val body = httpGetText(ENDPOINT + base) ?: return null
        return parse(body, base)
    }

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

    private val RATE_ENTRY =
        Regex("\"([A-Za-z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?(?:[eE][-+]?[0-9]+)?)")

    private val BASE_ENTRY = Regex("\"base_code\"\\s*:\\s*\"([A-Za-z]{3})\"")

    private val UPDATED_ENTRY = Regex("\"time_last_update_utc\"\\s*:\\s*\"([^\"]*)\"")
}