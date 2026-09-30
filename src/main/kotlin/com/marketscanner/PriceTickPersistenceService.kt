package com.marketscanner

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class PriceTickPersistenceService(
    private val priceTickRepository: PriceTickRepository,
) {
    /**
     * This is the exact spot where a blocking JDBC call would ruin the entire reactive chain: even a single blocking
     * database call here would force the pipeline off the event-loop and stall the stream. The repository must stay
     * fully reactive end-to-end for the service to remain non-blocking.
     */
    fun save(priceTick: PriceTick): Mono<PriceTick> {
        return priceTickRepository.save(PriceTickEntity.fromDomain(priceTick))
            .map { it.toDomain() }
    }

    fun findBySymbol(symbol: String, page: Int = 0, size: Int = 20): Flux<PriceTick> {
        return priceTickRepository.findBySymbolOrderByTimestampDesc(symbol, PageRequest.of(page, size))
            .map { it.toDomain() }
    }
}
