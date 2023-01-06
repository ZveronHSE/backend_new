package ru.zveron.service

import com.google.protobuf.Empty
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.contract.parameter.Type
import ru.zveron.contract.parameter.parameterValueRequest
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.exception.CategoryException
import ru.zveron.exception.LotException
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.repository.ParameterFromTypeRepository
import ru.zveron.util.CreateEntitiesUtils.mockParameterRequest
import ru.zveron.util.GeneratorUtils.generateInt
import ru.zveron.util.GeneratorUtils.generateString
import java.time.Instant
import java.util.*

internal class ParameterServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var parameterService: ParameterService

    @Autowired
    lateinit var categoryService: CategoryService

    @Autowired
    lateinit var parameterFromTypeRepository: ParameterFromTypeRepository

    companion object : KLogging() {
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

    @Test
    fun `ValidateValuesForParameter Correct request with all type parameters`(): Unit = runBlocking {
        val parameters = parameterFromTypeRepository.getAllByCategoryAndLotForm(
            Category(CATEGORY_ID_CAT, ""),
            LotForm(LOT_FORM_ID, "", "")
        ).toResponse()

        val parameterValues = mutableMapOf<Int, String>()

        for (parameter in parameters.parametersList) {
            if (parameter.valuesCount == 0) {
                val value = when (parameter.type) {
                    Type.STRING -> generateString()
                    Type.INT -> generateInt()
                    Type.DATE -> Date.from(Instant.now()).toInstant()
                    else -> {}
                }

                parameterValues[parameter.id] = value.toString()
            } else {
                parameterValues[parameter.id] = parameter.valuesList.random()
            }
        }

        val request = parameterValueRequest {
            categoryId = CATEGORY_ID_CAT
            lotFormId = LOT_FORM_ID
            this.parameterValues.putAll(parameterValues)
        }

        val response = parameterService.validateValuesForParameters(request)

        response shouldBe Empty.getDefaultInstance()
    }

    @Test
    fun `ValidateValuesForParameter Should throw exception for a required parameter that has not been filled in`(): Unit =
        runBlocking {

        }

    @Test
    fun `ValidateValuesForParameter Should throw exception for incorrect value for type Int parameter`(): Unit =
        runBlocking {

        }

    @Test
    fun `ValidateValuesForParameter Should throw exception for incorrect value for type String parameter`(): Unit =
        runBlocking {

        }

    @Test
    fun `ValidateValuesForParameter Should throw exception for incorrect value for type Date parameter`(): Unit =
        runBlocking {

        }

    @Test
    fun `ValidateValuesForParameter Extra parameters, should throw exception`(): Unit = runBlocking {

    }
}