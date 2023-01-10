package ru.zveron.repository

import io.grpc.Status
import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Lot
import ru.zveron.exception.LotException

@JvmDefaultWithCompatibility
interface LotRepository : JpaRepository<Lot, Long> {
    fun findAllBySellerIdOrderByDateCreationDesc(id: Long): List<Lot>

    fun findByIdOrThrow(id: Long): Lot =
        findById(id).orElseThrow { LotException(Status.NOT_FOUND, "Объявления с id=$id не существует") }
}