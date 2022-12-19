package ru.zveron.service

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import ru.zveron.AddRequest
import ru.zveron.BlacklistCrudServiceGrpcKt
import ru.zveron.BlacklistProfile
import ru.zveron.DeleteAllRecordsWhereUserBlocksRequest
import ru.zveron.DeleteAllRecordsWhereUserIsBlockedRequest
import ru.zveron.DeleteRequest
import ru.zveron.ExistInBlacklistRequest
import ru.zveron.ExistInBlacklistResponse
import ru.zveron.GetListRequest
import ru.zveron.GetListResponse
import ru.zveron.entity.BlacklistRecord
import ru.zveron.exception.BlacklistException
import ru.zveron.repository.BlacklistRepository

@GrpcService
class BlacklistCrudService : BlacklistCrudServiceGrpcKt.BlacklistCrudServiceCoroutineImplBase() {

    @Autowired
    lateinit var blacklistRepository: BlacklistRepository

    override suspend fun existInBlacklist(request: ExistInBlacklistRequest): ExistInBlacklistResponse =
        ExistInBlacklistResponse
            .newBuilder()
            .setExists(
                blacklistRepository.existsById_ReportedIdAndId_ReporterId(
                    reportedId = request.targetUserId,
                    reporterId = request.ownerId
                )
            )
            .build()

    override suspend fun getList(request: GetListRequest): GetListResponse =
        GetListResponse
            .newBuilder()
            .addAllBlacklistRecords(
                blacklistRepository.getById_ReporterId(request.id)
                    .map { BlacklistProfile.newBuilder().setId(it.id.reportedId).build() })
            .build()

    override suspend fun add(request: AddRequest): Empty {
        if (request.ownerId == request.targetUserId) {
            throw BlacklistException("Нельзя добавить себя в черный список")
        }

        blacklistRepository.save(BlacklistRecord(BlacklistRecord.BlacklistKey(request.ownerId, request.targetUserId)))
        return Empty.getDefaultInstance()
    }

    override suspend fun delete(request: DeleteRequest): Empty {
        if (request.ownerId == request.deletedUserId) {
            throw BlacklistException("Нельзя удалить себя из черного списка")
        }

        blacklistRepository.delete(BlacklistRecord(BlacklistRecord.BlacklistKey(request.ownerId, request.deletedUserId)))
        return Empty.getDefaultInstance()
    }

    @Transactional
    override suspend fun deleteAllRecordsWhereUserBlocks(request: DeleteAllRecordsWhereUserBlocksRequest): Empty = Empty.getDefaultInstance().also {
        blacklistRepository.deleteAllById_ReporterId(request.ownerId)
    }

    @Transactional
    override suspend fun deleteAllRecordsWhereUserIsBlocked(request: DeleteAllRecordsWhereUserIsBlockedRequest): Empty = Empty.getDefaultInstance().also {
        blacklistRepository.deleteAllById_ReportedId(request.deletedUserId)
    }
}