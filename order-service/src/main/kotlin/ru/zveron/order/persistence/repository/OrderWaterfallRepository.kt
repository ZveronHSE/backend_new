package ru.zveron.order.persistence.repository

import org.springframework.data.repository.Repository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import ru.zveron.order.persistence.entity.OrderLot

interface OrderWaterfallRepository: Repository<OrderLot, Long> {

    suspend fun  findAll(lastId: Long, pageSize: Int)
}