package ru.zveron.database

import mu.KLogging
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.transaction.annotation.Transactional
import ru.zveron.service.PossibleCustomerService
import java.time.Instant
import java.time.temporal.ChronoUnit

@Configuration
@EnableScheduling
class PurchaseCron(
    private val possibleCustomerService: PossibleCustomerService
) {
    companion object : KLogging()

    /**
     * Крон отрабатывает раз в 2 месяца в 00:00, удаляет все заявки на покупку старше 30 Дней
     */
    @Scheduled(cron = "0 0 0 ? */2 ?", zone = "Europe/Kaliningrad")
    @Transactional
    fun scheduleDeleteExpiredTokens() {
        logger.info("Начинаю чистить заявки на покупку")

        val size = possibleCustomerService.deleteAllByDateOfConversationBeginningLessThan(
            Instant.now().minus(30, ChronoUnit.DAYS)
        )

        logger.info("Почистил $size заявок")
    }
}