package ru.zveron.util

import io.grpc.Status
import ru.zveron.exception.LotException
import ru.zveron.grpc.configuration.AuthorizedProfileElement
import kotlin.coroutines.CoroutineContext

object UserUtil {

    fun getUserId(required: Boolean = false, coroutineContext: CoroutineContext): Long {
        val userId = coroutineContext[AuthorizedProfileElement]?.id

        if (required && userId == null) {
            throw LotException(Status.UNAUTHENTICATED, "user should be authorized for this endpoint")
        }

        return userId ?: 0
    }
}