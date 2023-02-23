package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.DataBaseApplicationTest
import ru.zveron.contract.parameter.internal.parameterValueRequest
import ru.zveron.contract.parameter.model.Type
import ru.zveron.entity.Category
import ru.zveron.entity.LotForm
import ru.zveron.exception.CategoryException
import ru.zveron.exception.ParameterException
import ru.zveron.mapper.ParameterMapper.toResponse
import ru.zveron.repository.ParameterFromTypeRepository
import ru.zveron.util.GeneratorUtils
import ru.zveron.util.GeneratorUtils.buildMapParameterValues


internal class ParameterServiceTest : DataBaseApplicationTest() {
    @Autowired
    lateinit var parameterService: ParameterService

    @Autowired
    lateinit var parameterFromTypeRepository: ParameterFromTypeRepository

    companion object : KLogging() {
        const val ID_UNKNOWN = 100500
        const val UNKNOWN_VALUE = 124323543
        const val CATEGORY_ID_ROOT = 1
        const val CATEGORY_ID_CAT = 4
        const val LOT_FORM_ID = 1
        const val LOT_FORM_ID_WITHOUT_PARAMETERS = 2
    }

    @Test
    fun `GetAllParameters Correct get all parameters by category and lotform`(): Unit = runBlocking {
        val responseExpected = parameterFromTypeRepository.getAllByCategoryAndLotForm(
            Category(CATEGORY_ID_CAT, ""),
            LotForm(LOT_FORM_ID, Category(CATEGORY_ID_ROOT, ""), "")
        )

        val responseActual = parameterService.getAllParameters(CATEGORY_ID_CAT, LOT_FORM_ID)

        responseActual shouldBe responseExpected
    }

    @Test
    fun `GetAllParameters Correct get zero parameters, if doesnt exists by category and lotform`(): Unit =
        runBlocking {
            val response = parameterService.getAllParameters(CATEGORY_ID_CAT, LOT_FORM_ID_WITHOUT_PARAMETERS)

            response.size shouldBe 0
        }

    @Test
    fun `GetAllParameters Should throw exception, if unknown id for category`(): Unit = runBlocking {
        shouldThrow<CategoryException> { parameterService.getAllParameters(ID_UNKNOWN, LOT_FORM_ID) }
    }

    @Test
    fun `GetAllParameters Should throw exception, if unknown id for lotform`(): Unit = runBlocking {
        shouldThrow<CategoryException> { parameterService.getAllParameters(CATEGORY_ID_CAT, ID_UNKNOWN) }
    }

    @Test
    fun `ValidateValuesForParameter Correct request with all type parameters`(): Unit = runBlocking {
        val parameters = parameterFromTypeRepository.getAllByCategoryAndLotForm(
            Category(CATEGORY_ID_CAT, ""),
            LotForm(LOT_FORM_ID, Category(CATEGORY_ID_ROOT, ""), "")
        ).toResponse().parametersList

        val parameterValues = parameters.buildMapParameterValues()

        val request = parameterValueRequest {
            categoryId = CATEGORY_ID_CAT
            lotFormId = LOT_FORM_ID
            this.parameterValues.putAll(parameterValues)
        }

        val response = parameterService.validateValuesForParameters(request)

        response shouldBe Unit
    }

    @Test
    fun `ValidateValuesForParameter Should throw exception for a required parameter that has not been filled in`(): Unit =
        runBlocking {
            val parameters = parameterFromTypeRepository.getAllByCategoryAndLotForm(
                Category(CATEGORY_ID_CAT, ""),
                LotForm(LOT_FORM_ID, Category(CATEGORY_ID_ROOT, ""), "")
            ).toResponse().parametersList

            val parameterValues = parameters.buildMapParameterValues()

            var parameterIdWhereShouldBeIncorrect = -1
            for (parameter in parameters) {
                if (parameter.isRequired) {
                    parameterIdWhereShouldBeIncorrect = parameter.id
                    break;
                }
            }

            // Чтобы проверить, что подготовка к тесту прошла успешно, айдишник должен быть инициализирован
            parameterIdWhereShouldBeIncorrect shouldNotBe -1

            parameterValues.remove(parameterIdWhereShouldBeIncorrect)

            val request = parameterValueRequest {
                categoryId = CATEGORY_ID_CAT
                lotFormId = LOT_FORM_ID
                this.parameterValues.putAll(parameterValues)
            }
            shouldThrow<ParameterException> { parameterService.validateValuesForParameters(request) }
        }


    @ParameterizedTest
    @EnumSource(Type::class, mode = EnumSource.Mode.EXCLUDE, names = ["UNRECOGNIZED"])
    fun `ValidateValuesForParameter Should throw exception for incorrect value for each type parameter`(type: Type): Unit =
        runBlocking {
            val parameters = parameterFromTypeRepository.getAllByCategoryAndLotForm(
                Category(CATEGORY_ID_CAT, ""),
                LotForm(LOT_FORM_ID, Category(CATEGORY_ID_ROOT, ""), "")
            ).toResponse().parametersList

            val parameterValues = parameters.buildMapParameterValues()

            var parameterIdWhereShouldBeIncorrect = -1
            for (parameter in parameters) {
                when (parameter.type) {
                    type -> {
                        parameterIdWhereShouldBeIncorrect = parameter.id
                        break;
                    }

                    else -> {}
                }
            }

            // Чтобы проверить, что подготовка к тесту прошла успешно, айдишник должен быть инициализирован
            parameterIdWhereShouldBeIncorrect shouldNotBe -1

            parameterValues[parameterIdWhereShouldBeIncorrect] = UNKNOWN_VALUE.toString()

            val request = parameterValueRequest {
                categoryId = CATEGORY_ID_CAT
                lotFormId = LOT_FORM_ID
                this.parameterValues.putAll(parameterValues)
            }
            shouldThrow<ParameterException> { parameterService.validateValuesForParameters(request) }
        }

    @Test
    fun `ValidateValuesForParameter Extra parameters, should throw exception`(): Unit = runBlocking {
        val parameters = parameterFromTypeRepository.getAllByCategoryAndLotForm(
            Category(CATEGORY_ID_CAT, ""),
            LotForm(LOT_FORM_ID, Category(CATEGORY_ID_ROOT, ""), "")
        ).toResponse().parametersList

        val parameterValues = parameters.buildMapParameterValues()
        parameterValues[ID_UNKNOWN] = UNKNOWN_VALUE.toString()

        val request = parameterValueRequest {
            categoryId = CATEGORY_ID_CAT
            lotFormId = LOT_FORM_ID
            this.parameterValues.putAll(parameterValues)
        }

        shouldThrow<ParameterException> { parameterService.validateValuesForParameters(request) }
    }

    @Test
    fun `GetAllParametersById Get parameters by Id`(): Unit = runBlocking {
        // Знаем, что в БД есть эти параметры гарантированно
        val ids = listOf(1, 4, 5)
        val parameters = parameterService.getAllParametersById(ids)
        parameters.size shouldBe ids.size

        parameters.map { it.id } shouldContainExactlyInAnyOrder ids
    }

    @Test
    fun `GetAllParametersById Should throw exception if get not valid identificators`(): Unit = runBlocking {
        val ids = List(4) { -GeneratorUtils.generateInt() }

        shouldThrow<ParameterException> { parameterService.getAllParametersById(ids) }
    }
}