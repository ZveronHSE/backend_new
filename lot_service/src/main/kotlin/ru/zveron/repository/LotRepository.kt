package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.zveron.entity.Lot

@Repository
interface LotRepository : JpaRepository<Lot, Long> {

    @Modifying
    @Query("UPDATE Lot l set l.seller = NULL WHERE l.seller.id = ?1")
    fun setSellerToNullByItsId(id: Long)
}