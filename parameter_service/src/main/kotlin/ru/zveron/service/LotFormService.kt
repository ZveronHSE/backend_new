package ru.zveron.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.zveron.entity.LotForm
import ru.zveron.repository.LotFormRepository
import ru.zveron.util.ValidateUtils.validatePositive

@Service
class LotFormService(
    private val lotFormRepository: LotFormRepository
) {
    @Cacheable("lotForms")
    fun getLotFormsByCategoryId(id: Int): List<LotForm> {
        id.validatePositive("categoryId")

        return lotFormRepository.getLotFormsByCategory_Id(id)
    }
}
