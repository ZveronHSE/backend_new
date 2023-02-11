package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotParameter

interface LotParameterRepository : JpaRepository<LotParameter, Long> {
    fun deleteByLot_Id(id: Long)
}