package ru.zveron.service.api.address

import ru.zveron.contract.AddressRequest
import ru.zveron.contract.AddressResponse

interface AddressService {

    suspend fun getById(id: Long): AddressResponse

    suspend fun saveIfNotExists(request: AddressRequest): AddressResponse
}