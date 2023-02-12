package ru.zveron.service.client.lot

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.profileLotsRequest

@Service
class LotGrpcClient : LotClient {

    @GrpcClient("lot-service")
    lateinit var service: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub

    override suspend fun getLotsBySellerId(sellerId: Long, userId: Long) =
        service.getLotsBySellerId(profileLotsRequest {
            this.sellerId = sellerId
            this.userId = userId
        })
}