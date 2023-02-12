package ru.zveron.authservice.cron

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import ru.zveron.authservice.persistence.SessionStorage

open class SessionCronScheduler(
    private val sessionStorage: SessionStorage,
) {

    @Scheduled(fixedRateString = "\${zveron.cron.session.rate-in-ms}")
    fun deleteExpired() {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            sessionStorage.deleteExpired()
        }
    }
}
