package ru.zveron.client.lot

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import ru.zveron.contract.core.Lot
import ru.zveron.contract.lot.LotInternalServiceGrpcKt
import ru.zveron.contract.lot.lotsIdRequest

@Component
class LotGrpcClient : LotClient {

    @GrpcClient("lot-client")
    lateinit var client: LotInternalServiceGrpcKt.LotInternalServiceCoroutineStub

    override suspend fun getLotsById(lotIds: List<Long>): MutableList<Lot> =
        client.getLotsById(lotsIdRequest { this.lotIds.addAll(lotIds) }).lotsList
}