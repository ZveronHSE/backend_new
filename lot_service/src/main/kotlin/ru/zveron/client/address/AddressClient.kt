package ru.zveron.client.address

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Service
import ru.zveron.contract.AddressServiceGrpcKt
import ru.zveron.contract.addressIdRequest
import ru.zveron.contract.addressRequest
import ru.zveron.contract.lot.FullAddress
import ru.zveron.exception.LotException
import ru.zveron.mapper.LotMapper.toAddress
import ru.zveron.model.Address

@Service
class AddressClient(
    private val addressStub: AddressServiceGrpcKt.AddressServiceCoroutineStub
) {
    suspend fun saveAddressIfNotExists(address: FullAddress): Address {
        val request = addressRequest {
            address.takeIf { it.hasRegion() }?.let { region = it.region }
            address.takeIf { it.hasDistrict() }?.let { district = it.district }
            address.takeIf { it.hasTown() }?.let { town = it.town }
            street = address.street
            house = address.house
            longitude = address.longitude
            latitude = address.latitude
        }

        return try {
            val response = addressStub.saveAddressIfNotExists(request)

            response.toAddress()
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get address id longitude: ${address.longitude} and latitude: ${address.latitude}. Status: ${ex.status.description}"
            )
        }
    }

    suspend fun getAddressById(id: Long): Address {
        val request = addressIdRequest { this.id = id }

        return try {
            val response = addressStub.getAddress(request)

            response.toAddress()
        } catch (ex: StatusException) {
            throw LotException(
                Status.INTERNAL,
                "Can't get address by id: $id. Status: ${ex.status.description}"
            )
        }
    }
}
