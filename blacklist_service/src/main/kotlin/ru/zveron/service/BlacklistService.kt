package ru.zveron.service

import com.google.protobuf.Empty
import org.springframework.stereotype.Service
import ru.zveron.client.profile.ProfileClient
import ru.zveron.contract.blacklist.AddToBlacklistRequest
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.contract.blacklist.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.contract.blacklist.DeleteFromBlacklistRequest
import ru.zveron.contract.blacklist.ExistInBlacklistRequest
import ru.zveron.contract.blacklist.ExistInBlacklistResponse
import ru.zveron.contract.blacklist.GetBlacklistResponse
import ru.zveron.contract.blacklist.existInBlacklistResponse
import ru.zveron.contract.blacklist.getBlacklistResponse
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.mapper.ProfileMapper.toResponse
import ru.zveron.repository.BlacklistRepository

@Service
class BlacklistService(
    private var blacklistRepository: BlacklistRepository,
    private val profileClient: ProfileClient,
) {

    suspend fun existInBlacklist(request: ExistInBlacklistRequest): ExistInBlacklistResponse =
        existInBlacklistResponse {
            exists = blacklistRepository.existsById_OwnerUserIdAndId_ReportedUserId(
                ownerUserId = request.ownerId,
                reportedUserId = request.targetUserId
            )
        }

    suspend fun getBlacklist(authorizedProfileId: Long): GetBlacklistResponse {
        val ids = blacklistRepository.getAllById_OwnerUserId(authorizedProfileId).map { it.id.reportedUserId }
        val profiles = profileClient.getProfilesSummary(ids).profilesList.map { it.toResponse() }

        return getBlacklistResponse {
            blacklistUsers.addAll(profiles)
        }
    }

    suspend fun addToBlacklist(request: AddToBlacklistRequest, authorizedProfileId: Long): Empty =
        request.takeUnless { request.id == authorizedProfileId }?.let {
            blacklistRepository.save(
                BlacklistRecord(
                    BlacklistRecord.BlacklistKey(
                        authorizedProfileId,
                        request.id
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw BlacklistException("Нельзя добавить себя в черный список")

    suspend fun deleteFromBlacklist(request: DeleteFromBlacklistRequest, authorizedProfileId: Long): Empty =
        request.takeUnless { it.id == authorizedProfileId }?.let {
            blacklistRepository.delete(
                BlacklistRecord(
                    BlacklistRecord.BlacklistKey(
                        authorizedProfileId,
                        request.id
                    )
                )
            )
            Empty.getDefaultInstance()
        } ?: throw BlacklistException("Нельзя удалить себя из черного списка")

    suspend fun deleteAllRecordsWhereUserBlocks(request: DeleteAllRecordsWhereUserBlocksRequest): Empty =
        Empty.getDefaultInstance().also {
            blacklistRepository.deleteAllById_OwnerUserId(request.ownerId)
        }

    suspend fun deleteAllRecordsWhereUserIsBlocked(request: DeleteAllRecordsWhereUserIsBlockedRequest): Empty =
        Empty.getDefaultInstance().also {
            blacklistRepository.deleteAllById_ReportedUserId(request.deletedUserId)
        }
}