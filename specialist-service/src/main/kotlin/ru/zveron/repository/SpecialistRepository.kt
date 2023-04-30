package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import ru.zveron.entity.Specialist

interface SpecialistRepository : JpaRepository<Specialist, Long> {

    @Modifying
    @Query("UPDATE Specialist s set s.description = :description WHERE s.id = :id")
    fun setDescription(id: Long, description: String)

    @Modifying
    @Query(
        """
        UPDATE Specialist s 
        set s.name = :name, s.surname = :surname, s.patronymic = :patronymic
        WHERE s.id = :id
        """
    )
    fun editName(id: Long, name: String, surname: String, patronymic: String)
}