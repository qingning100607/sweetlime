package com.qingning.sweetlime.core.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CurrencyRatesTest {

    private val body = """
        {"result":"success","base_code":"USD",
         "time_last_update_utc":"Mon, 01 Jan 2026 00:00:01 +0000",
         "rates":{"USD":1,"CNY":7.2,"EUR":0.9}}
    """.trimIndent()

    @Test
    fun parsesRates() {
        val rates = CurrencyApi.parse(body, "CNY")
        assertNotNull(rates)
        assertEquals("USD", rates.base)
        assertEquals(7.2, rates.rates["CNY"]!!, 1e-9)
    }

    @Test
    fun fallsBackToGivenBase() {
        val rates = CurrencyApi.parse("{\"rates\":{\"CNY\":7.2}}", "CNY")
        assertNotNull(rates)
        assertEquals("CNY", rates.base)
    }

    @Test
    fun crossesRatesLocally() {
        val rates = CurrencyApi.parse(body, "USD")!!
        assertEquals(7.2, rates.crossRate("USD", "CNY")!!, 1e-9)
        assertEquals(1.0 / 7.2, rates.crossRate("CNY", "USD")!!, 1e-9)
        assertEquals(1.0, rates.crossRate("CNY", "CNY")!!, 1e-9)
        assertNull(rates.crossRate("USD", "JPY"))
    }

    @Test
    fun convertsAmounts() {
        val rates = CurrencyApi.parse(body, "USD")!!
        assertEquals(14.4, rates.convert(2.0, "USD", "CNY")!!, 1e-9)
        assertNull(rates.convert(1.0, "USD", "JPY"))
    }

    @Test
    fun rejectsGarbage() {
        assertNull(CurrencyApi.parse("not json", "CNY"))
    }
}
