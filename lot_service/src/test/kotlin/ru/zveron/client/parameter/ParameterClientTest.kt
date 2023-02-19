package ru.zveron.client.parameter

import com.google.protobuf.Empty
import io.grpc.Status
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.extension.ExtendWith
import ru.zveron.contract.parameter.internal.CategoryServiceGrpcKt
import ru.zveron.contract.parameter.internal.ParameterServiceGrpcKt
import ru.zveron.contract.parameter.internal.category
import ru.zveron.contract.parameter.internal.categoryTreeResponse
import ru.zveron.exception.LotException
import ru.zveron.test.util.GeneratorUtils.generateInt
import ru.zveron.test.util.GeneratorUtils.generateInts
import ru.zveron.test.util.GeneratorUtils.generateString


@ExtendWith(MockKExtension::class)
class ParameterClientTest {
    @InjectMockKs
    lateinit var parameterClient: ParameterClient

    @MockK
    lateinit var parameterStub: ParameterServiceGrpcKt.ParameterServiceCoroutineStub

    @MockK
    lateinit var categoryStub: CategoryServiceGrpcKt.CategoryServiceCoroutineStub


    @Test
    fun `GetTreeByCategory get correct answer for correct request`(): Unit = runBlocking {
        val categoryIds = listOf(1, 3, 5, 7, 8)

        coEvery {
            categoryStub.getCategoryTree(any(), any())
        } returns categoryTreeResponse {
            categories.addAll(categoryIds.map {
                category {
                    id = it
                    name = "name$it"
                }
            })
        }

        val response = parameterClient.getTreeByCategory(categoryIds[0])

        response shouldBe categoryIds
    }

    @Test
    fun `GetTreeByCategory throw exception, if got error from external service`(): Unit = runBlocking {
        coEvery {
            categoryStub.getCategoryTree(any(), any())
        } throws StatusException(Status.INTERNAL)

        shouldThrow<LotException> { parameterClient.getTreeByCategory(generateInt()) }
    }

    @Test
    fun `ValidateParameters get correct answer for correct request`(): Unit = runBlocking {
        val (categoryId, lotFormId) = generateInts(2)
        val parameters = generateInts(10).associateWith { it.toString() }

        coEvery {
            parameterStub.validateValuesForParameters(any(), any())
        } returns Empty.getDefaultInstance()

        assertDoesNotThrow { parameterClient.validateParameters(categoryId, lotFormId, parameters) }
    }

    @Test
    fun `ValidateParameters throw exception, if got error from external service`(): Unit = runBlocking {
        coEvery {
            parameterStub.validateValuesForParameters(any(), any())
        } throws StatusException(Status.INTERNAL)

        val (categoryId, lotFormId) = generateInts(2)
        val parameters = generateInts(10).associateWith { it.toString() }

        shouldThrow<LotException> { parameterClient.validateParameters(categoryId, lotFormId, parameters) }
    }

    @Test
    fun `ValidateParameters throw exception, if got exception validation from external service`(): Unit = runBlocking {
        val mockExceptionDescription = generateString(10)
        coEvery {
            parameterStub.validateValuesForParameters(any(), any())
        } throws StatusException(Status.INVALID_ARGUMENT.withDescription(mockExceptionDescription))

        val (categoryId, lotFormId) = generateInts(2)
        val parameters = generateInts(10).associateWith { it.toString() }

        val exception =
            shouldThrow<LotException> { parameterClient.validateParameters(categoryId, lotFormId, parameters) }
        exception.status.code shouldBe Status.INVALID_ARGUMENT.code
        exception.message shouldBe mockExceptionDescription
    }

    @Test
    fun `getInfoAboutCategory get correct answer for correct request`(): Unit = runBlocking {

    }

    @Test
    fun `getParametersById get correct answer for correct request`(): Unit = runBlocking {

    }
}