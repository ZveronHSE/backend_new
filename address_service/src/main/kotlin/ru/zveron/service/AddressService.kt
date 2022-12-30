package ru.zveron.service

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.AddressIdRequest
import ru.zveron.contract.AddressRequest
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.AddressServiceGrpcKt
import ru.zveron.mapper.AddressMapper.toEntity
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.repository.AddressRepository
import javax.persistence.EntityNotFoundException

@Suppress("BlockingMethodInNonBlockingContext")
@GrpcService
class AddressService(
    private val addressRepository: AddressRepository
) : AddressServiceGrpcKt.AddressServiceCoroutineImplBase() {

    override suspend fun getAddress(request: AddressIdRequest): AddressResponse {
        val address = addressRepository.findById(request.id)
            .orElseThrow { throw EntityNotFoundException("Address by id ${request.id} didn't find") }

        return address.toResponse()
    }

    override suspend fun saveAddressIfNotExists(request: AddressRequest): AddressResponse {
        val address = addressRepository.findByLongitudeAndLatitude(request.longitude, request.latitude)
            ?: addressRepository.save(request.toEntity())

        return address.toResponse()
    }
}