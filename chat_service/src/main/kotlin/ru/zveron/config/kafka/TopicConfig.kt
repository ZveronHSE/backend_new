package ru.zveron.config.kafka

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaAdmin

@Configuration
class TopicConfig {

    @Value("#{chatConfigBean.nodeUuidFormatted}")
    lateinit var instanceId: String

    @Bean
    fun kafkaAdmin(kafkaProperties: KafkaProperties): KafkaAdmin =
        KafkaAdmin(kafkaProperties.buildAdminProperties())

    @Bean
    fun instanceTopic(): NewTopic = NewTopic(instanceId, 1, 1)
}