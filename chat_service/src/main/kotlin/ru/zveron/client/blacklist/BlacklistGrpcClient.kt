package ru.zveron.client.blacklist

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import ru.zveron.contract.blacklist.BlacklistServiceInternalGrpcKt
import ru.zveron.contract.blacklist.existInBlacklistRequest
import ru.zveron.contract.blacklist.existInMultipleBlacklistsRequest

@Component
class BlacklistGrpcClient : BlacklistClient {

    @GrpcClient("blacklist-client")
    lateinit var client: BlacklistServiceInternalGrpcKt.BlacklistServiceInternalCoroutineStub

    override suspend fun existsInBlacklist(ownerId: Long, targetProfileId: Long) =
        client.existInBlacklist(existInBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetProfileId
        }).exists

    override suspend fun existsInMultipleBlacklists(targetProfileId: Long, ownersIds: List<Long>): List<Boolean> =
        client.existInMultipleBlacklists(existInMultipleBlacklistsRequest {
            this.targetUserId = targetProfileId
            this.ownersIds.addAll(ownersIds)
        }).existsList
}