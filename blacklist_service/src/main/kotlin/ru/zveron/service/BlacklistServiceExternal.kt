package ru.zveron.service

import com.google.protobuf.Empty
import io.grpc.Status
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.config.AuthorizedProfileElement
import ru.zveron.contract.blacklist.AddToBlacklistRequest
import ru.zveron.contract.blacklist.BlacklistServiceExternalGrpcKt
import ru.zveron.contract.blacklist.DeleteFromBlacklistRequest
import ru.zveron.contract.blacklist.GetBlacklistResponse
import ru.zveron.exception.BlacklistException
import kotlin.coroutines.coroutineContext

@GrpcService
class BlacklistServiceExternal(private val blacklistService: BlacklistService) :
    BlacklistServiceExternalGrpcKt.BlacklistServiceExternalCoroutineImplBase() {

    override suspend fun addToBlacklist(request: AddToBlacklistRequest): Empty =
        blacklistService.addToBlacklist(request, getAuthorizedProfileId())

    override suspend fun deleteFromBlacklist(request: DeleteFromBlacklistRequest): Empty =
        blacklistService.deleteFromBlacklist(request, getAuthorizedProfileId())

    override suspend fun getBlacklist(request: Empty): GetBlacklistResponse =
        blacklistService.getBlacklist(getAuthorizedProfileId())

    private suspend fun getAuthorizedProfileId() =
        coroutineContext[AuthorizedProfileElement]?.id ?: throw BlacklistException(
            "Authentication required",
            Status.UNAUTHENTICATED
        )
}