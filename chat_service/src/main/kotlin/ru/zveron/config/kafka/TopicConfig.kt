package ru.zveron.config.kafka

import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class TopicConfig {

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    lateinit var bootstrapAddress: String

    @Value("#{chatConfigBean.nodeUuidFormatted}")
    lateinit var instanceId: String

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val config = mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapAddress,
        )

        return KafkaAdmin(config)
    }

    @Bean
    fun instanceTopic(): NewTopic = NewTopic(instanceId, 1, 1)
}