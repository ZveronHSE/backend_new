package ru.zveron.service

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.blacklist.AddToBlacklistRequest
import ru.zveron.contract.blacklist.BlacklistServiceExternalGrpcKt
import ru.zveron.contract.blacklist.DeleteFromBlacklistRequest
import ru.zveron.contract.blacklist.GetBlacklistResponse
import ru.zveron.library.grpc.util.GrpcUtils
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
        GrpcUtils.getMetadata(coroutineContext, requiredAuthorized = true).profileId!!
}