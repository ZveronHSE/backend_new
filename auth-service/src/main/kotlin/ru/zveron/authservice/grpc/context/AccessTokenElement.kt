package ru.zveron.authservice.grpc.context

import kotlin.coroutines.CoroutineContext

class AccessTokenElement(val accessToken: String?) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<AccessTokenElement>
        get() = Key

    companion object Key : CoroutineContext.Key<AccessTokenElement>
}
