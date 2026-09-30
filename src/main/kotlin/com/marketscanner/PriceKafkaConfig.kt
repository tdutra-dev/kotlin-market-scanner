package com.marketscanner

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions
import java.util.Collections

@Configuration
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class PriceKafkaConfig(
    @Value("\${kafka.bootstrap-servers:localhost:9092}") private val bootstrapServers: String,
    @Value("\${kafka.topic:price-ticks}") private val topic: String,
    @Value("\${kafka.consumer-group:market-scanner-group}") private val consumerGroup: String,
) {

    @Bean
    fun kafkaSender(): KafkaSender<String, String> {
        val props = mutableMapOf<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = 3

        return KafkaSender.create(SenderOptions.create(props))
    }

    @Bean
    fun kafkaReceiver(): KafkaReceiver<String, String> {
        val props = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = consumerGroup
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name

        val receiverOptions = ReceiverOptions.create<String, String>(props)
            .subscription(Collections.singleton(topic))

        return KafkaReceiver.create(receiverOptions)
    }
}
