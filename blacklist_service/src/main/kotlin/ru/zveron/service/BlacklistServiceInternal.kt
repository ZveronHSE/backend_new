package ru.zveron.service

import com.google.protobuf.Empty
import com.google.protobuf.empty
import org.springframework.stereotype.Service
import ru.zveron.contract.blacklist.BlacklistServiceInternalGrpcKt
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.contract.blacklist.ExistInBlacklistRequest
import ru.zveron.contract.blacklist.ExistInBlacklistResponse

@Service
class BlacklistServiceInternal(private val blacklistService: BlacklistService) :
    BlacklistServiceInternalGrpcKt.BlacklistServiceInternalCoroutineImplBase() {

    override suspend fun deleteAllRecordsWhereUserBlocks(request: DeleteAllRecordsWhereUserBlocksRequest): Empty =
        blacklistService.deleteAllRecordsWhereUserBlocks(request).apply { empty {  } }

    override suspend fun deleteAllRecordsWhereUserIsBlocked(request: DeleteAllRecordsWhereUserIsBlockedRequest): Empty =
        blacklistService.deleteAllRecordsWhereUserIsBlocked(request).apply { empty {  } }

    override suspend fun existInBlacklist(request: ExistInBlacklistRequest): ExistInBlacklistResponse =
        blacklistService.existInBlacklist(request)
}