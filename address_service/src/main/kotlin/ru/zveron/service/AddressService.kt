package ru.zveron.service

import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import ru.zveron.contract.AddressIdRequest
import ru.zveron.contract.AddressRequest
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.AddressServiceGrpcKt
import ru.zveron.mapper.AddressMapper.toEntity
import ru.zveron.mapper.AddressMapper.toResponse
import ru.zveron.repository.AddressRepository
import javax.persistence.EntityNotFoundException

@GrpcService
class AddressService(
    private val addressRepository: AddressRepository
) : AddressServiceGrpcKt.AddressServiceCoroutineImplBase() {

    override suspend fun getAddress(request: AddressIdRequest): AddressResponse {
        val address = try {
            addressRepository.getReferenceById(request.id)
        } catch (ex: JpaObjectRetrievalFailureException) {
            throw EntityNotFoundException("Address by id ${request.id} didnt find")
        }

        return address.toResponse()
    }

    override suspend fun saveAddressIfNotExists(request: AddressRequest): AddressResponse {
        val address = if (addressRepository.existsByLongitudeAndLatitude(request.longitude, request.latitude)) {
            addressRepository.getByLongitudeAndLatitude(request.longitude, request.latitude)
        } else {
            addressRepository.save(request.toEntity())
        }

        return address.toResponse()
    }
}