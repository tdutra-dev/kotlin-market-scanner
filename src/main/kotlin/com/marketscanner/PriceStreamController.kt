package com.marketscanner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api")
class PriceStreamController(
    private val priceStreamService: PriceStreamService,
) {
    @Autowired(required = false)
    var priceKafkaConsumer: PriceKafkaConsumer? = null
    /**
     * Returning Flux<PriceTick> with the SSE media type tells Spring to emit an HTTP stream, so the browser or
     * client receives one event after another instead of waiting for a final JSON document.
     */
    @GetMapping(value = ["/prices/stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamPrices(
        @RequestParam(defaultValue = "BTCUSDT") symbol: String,
    ): Flux<PriceTick> {
        return priceStreamService.stream(symbol)
    }

    @GetMapping(value = ["/prices/kafka-stream"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamKafkaPrices(): Flux<PriceTick> {
        return priceKafkaConsumer?.consume() ?: Flux.empty()
    }
}
