package ru.zveron.service.presentation.api

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.config.AuthorizedProfileElement
import ru.zveron.exception.FavoritesException
import kotlin.coroutines.CoroutineContext

@Service
class SecuredServiceImpl : SecuredService {
    override fun CoroutineContext.getAuthorizedProfileId() =
        this[AuthorizedProfileElement]?.id
            ?: throw FavoritesException("Authentication required", Status.UNAUTHENTICATED)
}