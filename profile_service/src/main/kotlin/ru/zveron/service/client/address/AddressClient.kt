package ru.zveron.service.client.address

import ru.zveron.contract.address.AddressRequest
import ru.zveron.contract.address.AddressResponse

interface AddressClient {

    suspend fun getById(id: Long): AddressResponse

    suspend fun saveIfNotExists(request: AddressRequest): AddressResponse
}