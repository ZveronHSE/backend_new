package ru.zveron.config.kafka

import com.google.protobuf.util.JsonFormat
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.Serializer
import org.springframework.beans.factory.annotation.Value
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

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapAddress: String

    @Bean
    fun producerFactory(): ProducerFactory<Long, ChatRouteResponse> {
        val config = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
        )

        return DefaultKafkaProducerFactory(config, LongSerializer(), ChatRouteSerializer())
    }

    @Bean
    fun kafkaTemplate() = KafkaTemplate(producerFactory())

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