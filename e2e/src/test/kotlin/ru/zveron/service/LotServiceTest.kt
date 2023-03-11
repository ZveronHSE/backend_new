package ru.zveron.service

import com.google.protobuf.ByteString
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldNotThrow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.E2eTest
import ru.zveron.contract.apigateway.apiGatewayRequest

class LotServiceTest: E2eTest() {

    @Test
    fun `Service respond on cardLotGet`() {
        shouldNotThrow<StatusException> {
            runBlocking {
                assertCallSucceeded(apiGatewayRequest {
                    methodAlias = "cardLotGet"
                    requestBody = ByteString.copyFrom(getProfilePageRequest())
                })
            }
        }
    }

    private fun getProfilePageRequest() = """
        {
            "id": "1"
        }
    """.trimIndent().toByteArray()
}