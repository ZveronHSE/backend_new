package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotPhoto

interface LotPhotoRepository : JpaRepository<LotPhoto, Long> {
    fun deleteAllByLot_Id(id: Long)
}
