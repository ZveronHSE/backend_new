package ru.zveron.repository

import io.grpc.Status
import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotForm
import ru.zveron.exception.CategoryException

@JvmDefaultWithCompatibility
interface LotFormRepository : JpaRepository<LotForm, Int> {
    fun getLotFormByIdOrThrow(id: Int): LotForm = findById(id).orElseThrow {
        CategoryException(Status.NOT_FOUND, "Вид такого объявления c id=$id не был найден")
    }

    fun getLotFormsByCategory_Id(id: Int): List<LotForm>
}