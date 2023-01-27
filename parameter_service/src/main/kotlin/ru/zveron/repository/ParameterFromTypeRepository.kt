package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.entity.ParameterFromType

interface ParameterFromTypeRepository : JpaRepository<ParameterFromType, Int> {
    fun getAllByCategoryAndLotForm(category: Category, lotForm: LotForm): List<ParameterFromType>
}
