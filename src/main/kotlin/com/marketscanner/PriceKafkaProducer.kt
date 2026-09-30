package com.marketscanner

import kotlinx.serialization.json.Json
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord

@Service
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class PriceKafkaProducer(
    private val kafkaSender: KafkaSender<String, String>,
) {
    private val logger = LoggerFactory.getLogger(PriceKafkaProducer::class.java)

    /**
     * Kafka is the durable boundary for downstream consumers: we publish each aggregated tick with a JSON payload so
     * other systems can read it without depending on the in-memory reactive stream directly.
     */
    fun publish(priceTick: PriceTick): Mono<Void> {
        val payload = Json.encodeToString(PriceTickPayload.serializer(), priceTick.toPayload())
        val producerRecord = ProducerRecord("price-ticks", priceTick.symbol, payload)
        val record = SenderRecord.create(producerRecord, Unit)

        return kafkaSender.send(Mono.just(record))
            .doOnNext { result ->
                logger.info(
                    "Kafka published tick {} at {} offset {}",
                    priceTick.symbol,
                    priceTick.price,
                    result.recordMetadata().offset(),
                )
            }
            .then()
    }
}
