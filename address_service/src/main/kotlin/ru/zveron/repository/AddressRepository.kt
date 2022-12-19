package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.Address

@Repository
interface AddressRepository : JpaRepository<Address, Long> {
    fun existsByLongitudeAndLatitude(longitude: Double, latitude: Double): Boolean

    fun getByLongitudeAndLatitude(longitude: Double, latitude: Double): Address
}