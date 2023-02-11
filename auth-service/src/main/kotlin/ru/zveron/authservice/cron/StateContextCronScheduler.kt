package ru.zveron.authservice.cron

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import ru.zveron.authservice.persistence.FlowStateStorage

open class StateContextCronScheduler(
    private val flowStateStorage: FlowStateStorage
) {
    @Scheduled(fixedRateString = "\${zveron.cron.flow-context.rate-in-ms}")
    fun deleteExpired() {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scope.launch {
            flowStateStorage.deleteExpired()
        }
    }
}
