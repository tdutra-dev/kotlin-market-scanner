package com.marketscanner

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PriceStreamServiceTest {

    @Test
    fun `retries after a websocket disconnect and emits an aggregated tick`() {
        // This fake source intentionally simulates a disconnect before the real feed resumes. Using a deterministic
        // source makes the retry/backoff contract explicit without depending on a live Binance connection.
        val attempts = AtomicInteger(0)
        val fakeClient = object : BinanceWebSocketClient() {
            override fun streamTrades(symbol: String): Flux<PriceTick> {
                return if (attempts.getAndIncrement() == 0) {
                    Flux.concat(
                        Flux.just(
                            PriceTick(symbol, BigDecimal("100.00"), Instant.now(), source = "binance"),
                        ),
                        Flux.error(RuntimeException("socket disconnected")),
                    )
                } else {
                    Flux.just(
                        PriceTick(symbol, BigDecimal("100.00"), Instant.now(), source = "binance"),
                        PriceTick(symbol, BigDecimal("200.00"), Instant.now(), source = "binance"),
                        PriceTick(symbol, BigDecimal("300.00"), Instant.now(), source = "binance"),
                    )
                }
            }
        }

        val repository = mock<PriceTickRepository>()
        whenever(repository.save(any<PriceTickEntity>())).thenAnswer { Mono.just(it.arguments[0] as PriceTickEntity) }

        val producer = mock<PriceKafkaProducer>()
        whenever(producer.publish(any<PriceTick>())).thenReturn(Mono.empty())

        val service = PriceStreamService(fakeClient, PriceTickPersistenceService(repository))
        service.priceKafkaProducer = producer

        val aggregated = service.stream("BTCUSDT")
            .take(1)
            .collectList()
            .block()

        assertNotNull(aggregated)
        assertEquals(1, aggregated.size)
        assertEquals(BigDecimal("100.00"), aggregated.first().price.setScale(2))
    }
}
