package ru.zveron.client.lot

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.lotsIdRequest

@Service
class LotGrpcClient : LotClient {

    @GrpcClient("lot-service")
    lateinit var service: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub

    override suspend fun getLotsById(lotIds: List<Long>) =
        service.getLotsById(lotsIdRequest { this.lotIds.addAll(lotIds) }).lotsList
}