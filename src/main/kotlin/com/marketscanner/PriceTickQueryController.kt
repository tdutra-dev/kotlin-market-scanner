package com.marketscanner

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api")
class PriceTickQueryController(
    private val priceTickPersistenceService: PriceTickPersistenceService,
) {
    @GetMapping("/prices")
    fun queryPrices(
        @RequestParam(defaultValue = "BTCUSDT") symbol: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): Flux<PriceTick> {
        return priceTickPersistenceService.findBySymbol(symbol, page, size)
    }
}
