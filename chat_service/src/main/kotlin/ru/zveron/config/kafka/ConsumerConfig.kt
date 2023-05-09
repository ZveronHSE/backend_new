package ru.zveron.config.kafka

import com.google.protobuf.util.JsonFormat
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import ru.zveron.contract.chat.ChatRouteResponse
import java.lang.IllegalArgumentException
import java.nio.charset.Charset

@EnableKafka
@Configuration
class ConsumerConfig {

    @Bean
    fun consumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<Long, ChatRouteResponse> =
        DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties(), LongDeserializer(), ChatRouteDeserializer())

    @Bean
    fun listenerFactory(kafkaProperties: KafkaProperties): ConcurrentKafkaListenerContainerFactory<Long, ChatRouteResponse> =
        ConcurrentKafkaListenerContainerFactory<Long, ChatRouteResponse>().apply {
            consumerFactory = consumerFactory(kafkaProperties)
        }

    class ChatRouteDeserializer: Deserializer<ChatRouteResponse> {
        override fun deserialize(topic: String?, data: ByteArray?): ChatRouteResponse {
            data ?: throw IllegalArgumentException("Cannot deserialize null message")

            val response = ChatRouteResponse.newBuilder()
            JsonFormat
                .parser()
                .ignoringUnknownFields()
                .merge(data.toString(Charset.forName(Charsets.UTF_8.name())), response)

            return response.build()
        }
    }
}