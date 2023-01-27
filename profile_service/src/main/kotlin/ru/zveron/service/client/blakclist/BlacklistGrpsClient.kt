package ru.zveron.service.client.blakclist

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.BlacklistServiceGrpcKt
import ru.zveron.existInBlacklistRequest

@Service
class BlacklistGrpsClient : BlacklistClient {

    @GrpcClient("blacklist-service")
    lateinit var service: BlacklistServiceGrpcKt.BlacklistServiceCoroutineStub

    override suspend fun existsInBlacklist(ownerId: Long, targetUserId: Long) =
        service.existInBlacklist(existInBlacklistRequest {
            this.ownerId = ownerId
            this.targetUserId = targetUserId
        }).exists
}