package ru.zveron.service.api.lot

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.profileLotsRequest

@Service
class LotGrpcService : LotService{

    @GrpcClient("lot-service")
    lateinit var service: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub

    override suspend fun getLotsBySellerId(profileId: Long) = service.getLotsBySellerId(profileLotsRequest { id =  profileId})
}