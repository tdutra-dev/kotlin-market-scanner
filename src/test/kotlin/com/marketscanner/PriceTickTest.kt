package com.marketscanner

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PriceTickTest {
    @Test
    fun `fromBinancePayload creates a valid price tick`() {
        val payload = """{"symbol":"BTCUSDT","price":"42123.45","volume":"123456.78"}"""

        val tick = PriceTick.fromBinancePayload(payload)

        assertEquals("BTCUSDT", tick.symbol)
        assertEquals("42123.45", tick.price.toPlainString())
        assertEquals("binance", tick.source)
        assertNotNull(tick.volume)
        assertEquals(123456.78, tick.volume!!, 0.001)
    }
}
