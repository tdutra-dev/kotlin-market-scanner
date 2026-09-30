package com.marketscanner

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class PriceStreamControllerTest {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @MockBean
    private lateinit var priceStreamService: PriceStreamService

    @Test
    fun `sse endpoint emits at least one price tick`() {
        val tick = PriceTick(
            symbol = "BTCUSDT",
            price = BigDecimal("65000.12"),
            timestamp = Instant.now(),
            source = "test",
            volume = null,
        )

        whenever(priceStreamService.stream("BTCUSDT")).thenReturn(Flux.just(tick))

        val body = webTestClient.get()
            .uri("/api/prices/stream?symbol=BTCUSDT")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .returnResult(String::class.java)
            .responseBody
            .collectList()
            .block()

        assertNotNull(body)
        assertTrue(body!!.joinToString().contains("BTCUSDT"))
    }
}
