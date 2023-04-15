package ru.zveron.repository

import org.springframework.data.repository.CrudRepository
import ru.zveron.entity.SubwayStation

interface SubwayStationRepository: CrudRepository<SubwayStation, Long> {

    fun findAllByCity(city: String): List<SubwayStation>

    fun findById(id: Int): SubwayStation?

    fun findAllByIdIn(ids: List<Int>): List<SubwayStation>
}