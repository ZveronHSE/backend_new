package ru.zveron.repository

import io.grpc.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.Lot
import ru.zveron.exception.LotException

@JvmDefaultWithCompatibility
interface LotRepository : JpaRepository<Lot, Long> {
    fun findByIdOrThrow(id: Long): Lot =
        findById(id).orElseThrow { LotException(Status.NOT_FOUND, "Объявления с id=$id не существует") }

//    @Query("SELECT l FROM Lot l JOIN FETCH l.photos JOIN FETCH l.statistics JOIN FETCH l.parameters WHERE l.id = :id")
//    fun findByIdWithRelatedEntities(id: Long): Lot

    @Query(
        """
        select distinct l from Lot l
        inner join fetch l.photos lp 
        where l.id in :ids and lp.orderPhoto = 0
    """
    )
    fun findAllLotsByIds(ids: List<Long>): List<Lot>

    @Query(
        """
        select distinct l from Lot l
        inner join fetch l.photos lp 
        where l.sellerId = :sellerId and lp.orderPhoto = 0
        order by l.createdAt desc 
    """
    )
    fun findAllLotsBySellerId(sellerId: Long): List<Lot>
}