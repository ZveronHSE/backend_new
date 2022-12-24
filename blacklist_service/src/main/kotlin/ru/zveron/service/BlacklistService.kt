package ru.zveron.service

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.AddToBlacklistRequest
import ru.zveron.BlacklistServiceGrpcKt
import ru.zveron.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.DeleteFromBlacklistRequest
import ru.zveron.ExistInBlacklistRequest
import ru.zveron.ExistInBlacklistResponse
import ru.zveron.GetBlacklistRequest
import ru.zveron.GetBlacklistResponse
import ru.zveron.blacklistUser
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.existInBlacklistResponse
import ru.zveron.getBlacklistResponse
import ru.zveron.repository.BlacklistRepository

@GrpcService
class BlacklistService(private var blacklistRepository: BlacklistRepository) :
    BlacklistServiceGrpcKt.BlacklistServiceCoroutineImplBase() {

    override suspend fun existInBlacklist(request: ExistInBlacklistRequest): ExistInBlacklistResponse =
        existInBlacklistResponse {
            exists = blacklistRepository.existsById_OwnerUserIdAndId_ReportedUserId(
                ownerUserId = request.ownerId,
                reportedUserId = request.targetUserId
            )
        }

    override suspend fun getBlacklist(request: GetBlacklistRequest): GetBlacklistResponse =
        getBlacklistResponse {
            blacklistUsers.addAll(
                blacklistRepository.getAllById_OwnerUserId(request.id).map { blacklistUser { id = it.id.reportedUserId } })
        }

    override suspend fun addToBlacklist(request: AddToBlacklistRequest): Empty =
        request.takeUnless { request.ownerId == request.targetUserId }?.let {
            blacklistRepository.save(
                BlacklistRecord(
                    BlacklistRecord.BlacklistKey(
                        request.ownerId,
                        request.targetUserId
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw BlacklistException("Нельзя добавить себя в черный список")

    override suspend fun deleteFromBlacklist(request: DeleteFromBlacklistRequest): Empty =
        request.takeUnless { it.ownerId == it.deletedUserId }?.let {
            blacklistRepository.delete(
                BlacklistRecord(
                    BlacklistRecord.BlacklistKey(
                        request.ownerId,
                        request.deletedUserId
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw BlacklistException("Нельзя удалить себя из черного списка")

    override suspend fun deleteAllRecordsWhereUserBlocks(request: DeleteAllRecordsWhereUserBlocksRequest): Empty =
        Empty.getDefaultInstance().also {
            blacklistRepository.deleteAllById_OwnerUserId(request.ownerId)
        }

    override suspend fun deleteAllRecordsWhereUserIsBlocked(request: DeleteAllRecordsWhereUserIsBlockedRequest): Empty =
        Empty.getDefaultInstance().also {
            blacklistRepository.deleteAllById_ReportedUserId(request.deletedUserId)
        }
}