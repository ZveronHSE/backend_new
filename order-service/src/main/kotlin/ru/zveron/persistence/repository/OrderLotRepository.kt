package ru.zveron.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.persistence.entity.OrderLot

interface OrderLotRepository : CoroutineCrudRepository<OrderLot, Long> {

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun findAllByProfileId(profileId: Long): List<OrderLot>
}