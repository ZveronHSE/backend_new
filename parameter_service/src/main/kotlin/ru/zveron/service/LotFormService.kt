package ru.zveron.service

import io.grpc.Status
import org.springframework.stereotype.Service
import ru.zveron.entity.LotForm
import ru.zveron.exception.CategoryException
import ru.zveron.repository.LotFormRepository

@Service
class LotFormService(
    private val lotFormRepository: LotFormRepository
) {
    fun getLotFormByIdOrThrow(id: Int): LotForm = lotFormRepository.findById(id).orElseThrow {
        CategoryException(Status.NOT_FOUND, "Вид такого объявления c id=$id не был найден")
    }
}
