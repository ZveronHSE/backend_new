package ru.zveron.authservice.grpc.context

import kotlin.coroutines.CoroutineContext

class AccessTokenElement(val accessToken: String?) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<AccessTokenElement>

    override val key: CoroutineContext.Key<*>
        get() = Key

}
