package com.marketscanner

import org.springframework.data.domain.Pageable
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

interface PriceTickRepository : ReactiveCrudRepository<PriceTickEntity, Long> {
    fun findBySymbolOrderByTimestampDesc(symbol: String, pageable: Pageable): Flux<PriceTickEntity>
}
