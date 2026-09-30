package com.marketscanner

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class BinancePriceClient(
    private val webClientBuilder: WebClient.Builder,
) {
    private val webClient = webClientBuilder
        .baseUrl("https://api.binance.com")
        .build()

    /**
     * The client returns a single price observation for a symbol. We keep the external API call isolated here so
     * the controller and service can work with the business model instead of Binance-specific details.
     */
    fun fetchLatestPrice(symbol: String = "BTCUSDT"): Mono<PriceTick> {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/api/v3/ticker/price")
                    .queryParam("symbol", symbol)
                    .build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono(String::class.java)
            .map { responseBody -> PriceTick.fromBinancePayload(responseBody, "binance") }
    }
}
