package ru.zveron.order.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

object CoroutineUtil {
    suspend fun <T> withCancellableContext(
        block: suspend CoroutineScope.() -> T,
    ): T {
        return withContext(Dispatchers.Default + SupervisorJob(), block)
    }
}