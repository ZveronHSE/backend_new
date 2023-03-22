package ru.zveron.client.blacklist

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.blacklist.BlacklistServiceInternalGrpcKt
import ru.zveron.contract.blacklist.existInBlacklistRequest

@Service
class BlacklistGrpsClient : BlacklistClient {

    @GrpcClient("blacklist-service")
    lateinit var service: BlacklistServiceInternalGrpcKt.BlacklistServiceInternalCoroutineStub

    override suspend fun existsInBlacklist(ownerId: Long, targetUserId: Long) =
        service.existInBlacklist(existInBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetUserId
        }).exists
}