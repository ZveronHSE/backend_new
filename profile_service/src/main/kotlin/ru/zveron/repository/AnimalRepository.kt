package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Animal

interface AnimalRepository:  JpaRepository<Animal, Long>{
    fun findAllByProfileId(id: Long): List<Animal>
}