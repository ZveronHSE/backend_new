package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.entity.LotForm
import ru.zveron.exception.LotException
import ru.zveron.repository.LotFormRepository

internal class LotFormServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var lotFormService: LotFormService

    @Autowired
    lateinit var lotFormRepository: LotFormRepository

    @Test
    fun `Success get lot form by id`() {
        val lotForm = lotFormRepository.save(
            LotForm(
                form = "form",
                type = "type"
            )
        )

        val response = lotFormService.getLotFormByIdOrThrow(lotForm.id)

        response shouldBe lotForm
    }

    @Test
    fun `Dont get lot form by id, if it doesnt exists`() {
        val mockId = 1

        shouldThrow<LotException> { lotFormService.getLotFormByIdOrThrow(mockId) }
    }
}