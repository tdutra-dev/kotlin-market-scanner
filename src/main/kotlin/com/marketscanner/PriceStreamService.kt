package com.marketscanner

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@Service
class PriceStreamService(
    private val binanceWebSocketClient: BinanceWebSocketClient,
) {
    private val logger = LoggerFactory.getLogger(PriceStreamService::class.java)

    /**
     * The WebSocket stream is continuous, so we aggregate ticks into windows to produce a cleaner signal for clients.
     * `retryWhen` protects against temporary disconnects; `onErrorResume` is a last safety net for any fatal error.
     */
    fun stream(symbol: String = "BTCUSDT"): Flux<PriceTick> {
        return binanceWebSocketClient.streamTrades(symbol)
            .retryWhen(
                Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(5))
                    .doBeforeRetry { retrySignal ->
                        logger.warn(
                            "Binance websocket disconnected; retrying stream for {} (attempt {})",
                            symbol,
                            retrySignal.totalRetries() + 1,
                        )
                    }
            )
            .onErrorResume { error ->
                logger.warn("WebSocket stream ended after retries for {}, returning an empty stream.", symbol, error)
                Flux.empty()
            }
            .bufferTimeout(5, Duration.ofSeconds(5))
            .filter { it.isNotEmpty() }
            .map { aggregateWindow(it, symbol) }
    }

    private fun aggregateWindow(ticks: List<PriceTick>, symbol: String): PriceTick {
        val averagePrice = ticks.map { it.price }
            .fold(BigDecimal.ZERO) { acc, price -> acc + price }
            .divide(BigDecimal.valueOf(ticks.size.toLong()), 8, java.math.RoundingMode.HALF_UP)

        return PriceTick(
            symbol = symbol,
            price = averagePrice,
            timestamp = Instant.now(),
            source = "aggregated",
            volume = ticks.size.toDouble(),
        )
    }
}
