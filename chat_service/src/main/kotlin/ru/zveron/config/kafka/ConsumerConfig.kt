package ru.zveron.config.kafka

import com.google.protobuf.util.JsonFormat
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.LongDeserializer
import org.springframework.beans.factory.annotation.Value
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

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapAddress: String

    @Bean
    fun consumerFactory(): ConsumerFactory<Long, ChatRouteResponse> {
        val config = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
        )

        return DefaultKafkaConsumerFactory(config, LongDeserializer(), ChatRouteDeserializer())
    }

    @Bean
    fun listenerFactory(): ConcurrentKafkaListenerContainerFactory<Long, ChatRouteResponse> =
        ConcurrentKafkaListenerContainerFactory<Long, ChatRouteResponse>().apply {
            consumerFactory = consumerFactory()
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