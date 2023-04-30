package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Document
import ru.zveron.entity.Specialist

interface DocumentRepository : JpaRepository<Document, Long> {
    fun deleteAllBySpecialist(specialist: Specialist)
}