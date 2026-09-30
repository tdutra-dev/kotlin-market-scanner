package com.marketscanner

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import java.time.Duration

@Service
class PriceStreamService(
    private val binancePriceClient: BinancePriceClient,
) {
    private val logger = LoggerFactory.getLogger(PriceStreamService::class.java)

    /**
     * Flux is the right abstraction for a market stream because we can emit multiple price updates over time instead
     * of a single request/response. The `onBackpressureDrop` comment below is intentional: if the client is slower
     * than the producer, a bounded queue would be dangerous; dropping stale values is safer than letting memory grow.
     */
    fun stream(symbol: String = "BTCUSDT"): Flux<PriceTick> {
        return Flux.interval(Duration.ofSeconds(1))
            .flatMap { binancePriceClient.fetchLatestPrice(symbol) }
            .take(5)
            .doOnNext { tick ->
                logger.info("Emitting tick for {} at {}", tick.symbol, tick.price)
            }
            .onBackpressureDrop {
                logger.warn("Client is slower than the stream producer; dropping an outdated tick.")
            }
    }
}
