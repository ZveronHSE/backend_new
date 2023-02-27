package ru.zveron.service.presentation.api

import org.springframework.stereotype.Service
import ru.zveron.library.grpc.util.GrpcUtils
import kotlin.coroutines.CoroutineContext

@Service
class SecuredServiceImpl : SecuredService {
    override fun CoroutineContext.getAuthorizedProfileId() =
        GrpcUtils.getMetadata(this, requiredAuthorized = true).profileId!!
}