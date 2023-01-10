package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.zveron.entity.PossibleCustomer
import java.time.Instant

@Repository
interface PossibleCustomerRepository : JpaRepository<PossibleCustomer, Long> {

    @Modifying
    @Query("DELETE FROM PossibleCustomer c WHERE c.profile.id = ?1")
    fun deleteByCustomerId(id: Long)

    fun deleteAllByDateOfConversationBeginningLessThan(date: Instant): Long
}