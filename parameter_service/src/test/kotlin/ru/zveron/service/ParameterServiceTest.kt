package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.exception.CategoryException
import ru.zveron.exception.LotException
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.repository.ParameterFromTypeRepository
import ru.zveron.util.CreateEntitiesUtil.mockParameterRequest

internal class ParameterServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var parameterService: ParameterService

    @Autowired
    lateinit var lotFormService: LotFormService

    @Autowired
    lateinit var categoryService: CategoryService

    @Autowired
    lateinit var parameterFromTypeRepository: ParameterFromTypeRepository

    companion object {
        const val CATEGORY_ID_DOG = 3
        const val ID_UNKNOWN = 100500
        const val CATEGORY_ID_CAT = 4
        const val LOT_FORM_ID = 1
        const val LOT_FORM_ID_WITHOUT_PARAMETERS = 2
    }

    @Test
    fun `GetParametersByCategory Correct get all parameters by category and lotform`(): Unit = runBlocking {
        val responseExpected = parameterFromTypeRepository.getAllByCategoryAndLotForm(
            Category(CATEGORY_ID_CAT, ""),
            LotForm(LOT_FORM_ID, "", "")
        ).toResponse()

        val request = mockParameterRequest(CATEGORY_ID_CAT, LOT_FORM_ID)
        val responseActual = parameterService.getParametersByCategory(request)

        responseActual shouldBe responseExpected
    }

    @Test
    fun `GetParametersByCategory Correct get zero parameters, if doesnt exists by category and lotform`(): Unit =
        runBlocking {
            val request = mockParameterRequest(CATEGORY_ID_CAT, LOT_FORM_ID_WITHOUT_PARAMETERS)
            val response = parameterService.getParametersByCategory(request)

            response.parametersCount shouldBe 0
        }

    @Test
    fun `GetParametersByCategory Should throw exception, if unknown id for category`(): Unit = runBlocking {
        val request = mockParameterRequest(ID_UNKNOWN, LOT_FORM_ID)

        shouldThrow<CategoryException> { parameterService.getParametersByCategory(request) }
    }

    @Test
    fun `GetParametersByCategory Should throw exception, if unknown id for lotform`(): Unit = runBlocking {
        val request = mockParameterRequest(CATEGORY_ID_CAT, ID_UNKNOWN)

        shouldThrow<LotException> { parameterService.getParametersByCategory(request) }
    }
}