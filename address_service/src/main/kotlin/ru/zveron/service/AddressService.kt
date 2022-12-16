package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.Address
import ru.zveron.repository.AddressRepository

@Service
class AddressService(
    val addressRepository: AddressRepository
) {
    fun saveIfNotExists(address: Address): Address {
        // Мы проверяем уникальность адреса только по координатам, поэтому сначала проверим, что такой адрес существует,
        // если да, то мы просто возвращаем его, а не создаем.
        if (addressRepository.existsByLongitudeAndLatitude(address.longitude, address.latitude)) {
            return addressRepository.getByLongitudeAndLatitude(address.longitude, address.latitude)
        }

        return addressRepository.save(address)
    }

}