package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByLongitudeAndLatitude(longitude: Double, latitude: Double): Address?
}