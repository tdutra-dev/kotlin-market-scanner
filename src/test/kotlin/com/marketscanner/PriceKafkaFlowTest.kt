package com.marketscanner

import kotlinx.serialization.json.Json
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderRecord
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals

@Testcontainers
class PriceKafkaFlowTest {

    @Test
    fun `producer publishes a tick and consumer receives it end-to-end`() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable())

        val kafkaContainer = KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
        )
        kafkaContainer.start()

        val senderProps = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.ACKS_CONFIG to "all",
        )

        val receiverProps = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaContainer.bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to "market-scanner-kafka-flow-test",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
        )

        val sender = KafkaSender.create(SenderOptions.create<String, String>(senderProps))
        val receiver = KafkaReceiver.create(
            ReceiverOptions.create<String, String>(receiverProps).subscription(listOf("price-ticks"))
        )

        val tick = PriceTick(
            symbol = "BTCUSDT",
            price = BigDecimal("123.45"),
            timestamp = Instant.now(),
            source = "test",
            volume = 1.0,
        )
        val payload = Json.encodeToString(PriceTickPayload.serializer(), tick.toPayload())

        sender.send(Mono.just(SenderRecord.create(ProducerRecord("price-ticks", tick.symbol, payload), Unit)))
            .then()
            .block(Duration.ofSeconds(30))

        StepVerifier.create(
            receiver.receive()
                .take(1)
                .map { record ->
                    val payloadFromKafka = Json.decodeFromString(PriceTickPayload.serializer(), record.value())
                    val received = PriceTick.fromPayload(payloadFromKafka)
                    record.receiverOffset().acknowledge()
                    received
                }
        )
            .assertNext { received ->
                assertEquals(tick.symbol, received.symbol)
                assertEquals(tick.price.setScale(2), received.price.setScale(2))
            }
            .verifyComplete()

        kafkaContainer.stop()
    }
}
