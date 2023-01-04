package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.entity.ParameterFromType

@Repository
interface ParameterFromTypeRepository : JpaRepository<ParameterFromType, Long> {
    fun getAllByCategoryAndLotForm(category: Category, lotForm: LotForm): List<ParameterFromType>
}