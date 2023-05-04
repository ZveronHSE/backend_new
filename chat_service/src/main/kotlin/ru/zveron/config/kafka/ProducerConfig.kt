package ru.zveron.config.kafka

import com.google.protobuf.util.JsonFormat
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.Serializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import ru.zveron.contract.chat.ChatRouteResponse
import java.lang.IllegalArgumentException
import java.nio.charset.Charset

@Configuration
class ProducerConfig {

    @Bean
    fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<Long, ChatRouteResponse> =
        DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(), LongSerializer(), ChatRouteSerializer())

    @Bean
    fun kafkaTemplate(kafkaProperties: KafkaProperties) = KafkaTemplate(producerFactory(kafkaProperties))

    class ChatRouteSerializer: Serializer<ChatRouteResponse> {
        override fun serialize(topic: String?, data: ChatRouteResponse?): ByteArray {
            data ?: throw IllegalArgumentException("Cannot serialize null message")

            return JsonFormat
                .printer()
                .includingDefaultValueFields()
                .print(data)
                .toByteArray(Charset.forName(Charsets.UTF_8.name()))
        }
    }
}