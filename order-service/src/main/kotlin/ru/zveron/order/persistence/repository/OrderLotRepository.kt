package ru.zveron.order.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.order.persistence.entity.OrderLot

interface OrderLotRepository : CoroutineCrudRepository<OrderLot, Long> {

    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun findAllByProfileId(profileId: Long): List<OrderLot>
}