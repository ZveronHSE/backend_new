package ru.zveron.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.address.AddressIdRequest
import ru.zveron.contract.address.AddressRequest
import ru.zveron.contract.address.AddressResponse
import ru.zveron.contract.address.AddressServiceGrpcKt
import ru.zveron.mapper.AddressMapper.toEntity
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.repository.AddressRepository
import javax.persistence.EntityNotFoundException

@GrpcService
class AddressEntrypoint(
    private val addressRepository: AddressRepository
) : AddressServiceGrpcKt.AddressServiceCoroutineImplBase() {

    override suspend fun getAddress(request: AddressIdRequest): AddressResponse {
        if (request.id <= 0) {
            throw IllegalArgumentException("Address ID can't be less than 1")
        }

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