package ru.zveron.service.client.address

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Service
import ru.zveron.contract.AddressRequest
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.AddressServiceGrpcKt
import ru.zveron.contract.addressIdRequest

@Service
class AddressGrpcClient : AddressClient {

    @GrpcClient("address-service")
    lateinit var service: AddressServiceGrpcKt.AddressServiceCoroutineStub

    override suspend fun getById(id: Long): AddressResponse = service.getAddress(addressIdRequest { this.id = id })

    override suspend fun saveIfNotExists(request: AddressRequest) = service.saveAddressIfNotExists(request)
}
