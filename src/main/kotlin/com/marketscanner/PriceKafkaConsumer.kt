package com.marketscanner

import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.kafka.receiver.KafkaReceiver

@Service
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class PriceKafkaConsumer(
    private val kafkaReceiver: KafkaReceiver<String, String>,
) {
    private val logger = LoggerFactory.getLogger(PriceKafkaConsumer::class.java)

    /**
     * We read from the Kafka topic using `KafkaReceiver` and immediately convert each record back into the domain model.
     * This keeps the consumer stream reactive and lets the same `Flux<PriceTick>` abstraction be used elsewhere.
     */
    fun consume(): Flux<PriceTick> {
        return kafkaReceiver.receive()
            .map { record ->
                val payload = Json.decodeFromString(PriceTickPayload.serializer(), record.value())
                val tick = PriceTick.fromPayload(payload)
                record.receiverOffset().acknowledge()
                tick
            }
            .doOnNext { tick ->
                logger.info("Kafka consumed tick {} at {}", tick.symbol, tick.price)
            }
    }
}
