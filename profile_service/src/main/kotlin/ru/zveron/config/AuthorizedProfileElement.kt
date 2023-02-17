package ru.zveron.config

import kotlin.coroutines.CoroutineContext

class AuthorizedProfileElement(val id: Long?) : CoroutineContext.Element {
    companion object Key : CoroutineContext.Key<AuthorizedProfileElement>
    override val key: CoroutineContext.Key<*>
        get() = Key
}