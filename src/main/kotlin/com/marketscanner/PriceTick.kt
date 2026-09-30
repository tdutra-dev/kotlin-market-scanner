package com.marketscanner

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import java.time.Instant

@Serializable
private data class BinancePriceResponse(
    val symbol: String,
    val price: String,
    val volume: String? = null,
)

@Serializable
data class PriceTickPayload(
    val symbol: String,
    val price: String,
    val timestamp: String,
    val source: String? = null,
    val volume: Double? = null,
)

/**
 * Represents a single trade/price observation. The price is non-null because every tick must have a numeric value
 * to compute averages or compare prices; the optional fields are nullable because some APIs omit them.
 */
data class PriceTick(
    val symbol: String,
    val price: BigDecimal,
    val timestamp: Instant,
    val source: String? = null,
    val volume: Double? = null,
) {
    init {
        require(symbol.isNotBlank()) { "symbol cannot be blank" }
        require(price > BigDecimal.ZERO) { "price must be positive" }
    }

    companion object {
        /**
         * Parses the raw Binance payload into the domain model. We keep the vendor-specific DTO private so the
         * rest of the code speaks in terms of PriceTick instead of low-level JSON details.
         */
        fun fromBinancePayload(payload: String, source: String = "binance"): PriceTick {
            val response = Json { ignoreUnknownKeys = true }
                .decodeFromString<BinancePriceResponse>(payload)

            return PriceTick(
                symbol = response.symbol,
                price = response.price.toBigDecimal(),
                timestamp = Instant.now(),
                source = source,
                volume = response.volume?.toDouble(),
            )
        }

        fun fromPayload(payload: PriceTickPayload): PriceTick = PriceTick(
            symbol = payload.symbol,
            price = payload.price.toBigDecimal(),
            timestamp = Instant.parse(payload.timestamp),
            source = payload.source,
            volume = payload.volume,
        )
    }

    fun toPayload(): PriceTickPayload = PriceTickPayload(
        symbol = symbol,
        price = price.toPlainString(),
        timestamp = timestamp.toString(),
        source = source,
        volume = volume,
    )
}
