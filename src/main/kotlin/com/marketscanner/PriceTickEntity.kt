package com.marketscanner

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.Instant

@Table("price_ticks")
data class PriceTickEntity(
    @Id
    val id: Long? = null,
    val symbol: String,
    val price: BigDecimal,
    val timestamp: Instant,
    val source: String?,
    val volume: Double?,
) {
    companion object {
        fun fromDomain(priceTick: PriceTick): PriceTickEntity = PriceTickEntity(
            symbol = priceTick.symbol,
            price = priceTick.price,
            timestamp = priceTick.timestamp,
            source = priceTick.source,
            volume = priceTick.volume,
        )
    }

    fun toDomain(): PriceTick = PriceTick(
        symbol = symbol,
        price = price,
        timestamp = timestamp,
        source = source,
        volume = volume,
    )
}
