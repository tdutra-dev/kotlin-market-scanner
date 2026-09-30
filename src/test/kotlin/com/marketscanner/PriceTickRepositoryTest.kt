package com.marketscanner

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

@DataR2dbcTest
@ActiveProfiles("test")
class PriceTickRepositoryTest {

    @Autowired
    private lateinit var priceTickRepository: PriceTickRepository

    @Test
    fun `repository saves and page-fetches rows by symbol`() {
        val ticks = listOf(
            PriceTickEntity(
                symbol = "BTCUSDT",
                price = BigDecimal("100.00"),
                timestamp = Instant.now(),
                source = "test",
                volume = 1.0,
            ),
            PriceTickEntity(
                symbol = "BTCUSDT",
                price = BigDecimal("200.00"),
                timestamp = Instant.now().plusSeconds(5),
                source = "test",
                volume = 2.0,
            ),
        )

        StepVerifier.create(
            priceTickRepository.saveAll(Flux.fromIterable(ticks))
                .thenMany(priceTickRepository.findBySymbolOrderByTimestampDesc("BTCUSDT", PageRequest.of(0, 10)))
        )
            .expectNextCount(2)
            .verifyComplete()
    }
}
