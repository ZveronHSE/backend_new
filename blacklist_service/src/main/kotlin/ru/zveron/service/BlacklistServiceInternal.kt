package ru.zveron.service

import com.google.protobuf.Empty
import com.google.protobuf.empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.blacklist.BlacklistServiceInternalGrpcKt
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.contract.blacklist.ExistInBlacklistRequest
import ru.zveron.contract.blacklist.ExistInBlacklistResponse
import ru.zveron.contract.blacklist.ExistInMultipleBlacklistsRequest
import ru.zveron.contract.blacklist.ExistInMultipleBlacklistsResponse

@GrpcService
class BlacklistServiceInternal(private val blacklistService: BlacklistService) :
    BlacklistServiceInternalGrpcKt.BlacklistServiceInternalCoroutineImplBase() {

    override suspend fun deleteAllRecordsWhereUserBlocks(request: DeleteAllRecordsWhereUserBlocksRequest): Empty =
        blacklistService.deleteAllRecordsWhereUserBlocks(request).apply { empty {  } }

    override suspend fun deleteAllRecordsWhereUserIsBlocked(request: DeleteAllRecordsWhereUserIsBlockedRequest): Empty =
        blacklistService.deleteAllRecordsWhereUserIsBlocked(request).apply { empty {  } }

    override suspend fun existInBlacklist(request: ExistInBlacklistRequest): ExistInBlacklistResponse =
        blacklistService.existInBlacklist(request)

    override suspend fun existInMultipleBlacklists(request: ExistInMultipleBlacklistsRequest): ExistInMultipleBlacklistsResponse =
        blacklistService.existInMultipleBlacklists(request)
}