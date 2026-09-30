package com.marketscanner

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient
import reactor.core.publisher.Flux
import java.net.URI
import java.time.Instant

@Component
open class BinanceWebSocketClient {
    private val webSocketClient = ReactorNettyWebSocketClient()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * We connect directly to Binance's websocket endpoint and parse each trade event into the domain model. The
     * stream stays reactive end-to-end: the `Flux` emits PriceTick values as they arrive, instead of buffering the
     * whole response in memory before the service can process it.
     */
    open fun streamTrades(symbol: String = "BTCUSDT"): Flux<PriceTick> {
        val normalizedSymbol = symbol.uppercase()
        val uri = URI.create("wss://stream.binance.com:9443/ws/${normalizedSymbol.lowercase()}@trade")

        return Flux.create { sink ->
            val disposable = webSocketClient.execute(uri) { session ->
                session.receive()
                    .map { it.payloadAsText }
                    .map { payloadText ->
                        val trade = json.decodeFromString<BinanceTradePayload>(payloadText)
                        PriceTick(
                            symbol = trade.symbol.ifBlank { normalizedSymbol },
                            price = trade.price.toBigDecimal(),
                            timestamp = Instant.ofEpochMilli(trade.eventTime),
                            source = "binance",
                        )
                    }
                    .doOnNext { tick -> sink.next(tick) }
                    .doOnError { error -> sink.error(error) }
                    .doOnComplete { sink.complete() }
                    .then()
            }.subscribe()

            sink.onCancel { disposable.dispose() }
        }
    }
}

@Serializable
private data class BinanceTradePayload(
    @SerialName("s") val symbol: String,
    @SerialName("p") val price: String,
    @SerialName("E") val eventTime: Long,
)
