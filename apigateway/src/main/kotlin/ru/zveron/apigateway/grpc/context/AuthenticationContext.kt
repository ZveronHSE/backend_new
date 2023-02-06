package ru.zveron.apigateway.grpc.context

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class AuthenticationContext(var accessToken: String?) : ThreadContextElement<String?> {

    companion object Key : CoroutineContext.Key<AuthenticationContext> {
        suspend fun current() = coroutineContext[Key]?.accessToken
    }

    override val key: CoroutineContext.Key<*>
        get() = Key

    override fun updateThreadContext(context: CoroutineContext): String? {
        return accessToken
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: String?) {
        accessToken = oldState
    }
}
