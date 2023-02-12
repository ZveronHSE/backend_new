package ru.zveron.authservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import ru.zveron.authservice.cron.SessionCronScheduler
import ru.zveron.authservice.cron.StateContextCronScheduler
import ru.zveron.authservice.persistence.FlowStateStorage
import ru.zveron.authservice.persistence.SessionStorage

@Configuration
@EnableScheduling
class SchedulerConfiguration {

    @Bean
    fun sessionCronScheduler(sessionStorage: SessionStorage) = SessionCronScheduler(sessionStorage)

    @Bean
    fun flowStateCronScheduler(flowStateStorage: FlowStateStorage) = StateContextCronScheduler(flowStateStorage)
}
