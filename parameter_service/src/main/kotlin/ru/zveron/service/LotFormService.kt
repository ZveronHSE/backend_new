package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.LotForm
import ru.zveron.exception.LotException
import ru.zveron.repository.LotFormRepository

@Service
class LotFormService(
    private val lotFormRepository: LotFormRepository
) {
    fun getLotFormByIdOrThrow(id: Int): LotForm = lotFormRepository.findById(id).orElseThrow {
        LotException("Вид такого объявления по $id не был найден")
    }
}
