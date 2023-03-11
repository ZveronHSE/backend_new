package ru.zveron.service

import com.google.protobuf.ByteString
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldNotThrow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.E2eTest
import ru.zveron.contract.apigateway.apiGatewayRequest

class ParameterServiceTest: E2eTest() {

    @Test
    fun `Service respond on categoryRootGet`() {
        shouldNotThrow<StatusException> {
            runBlocking {
                assertCallSucceeded(apiGatewayRequest {
                    methodAlias = "categoryRootGet"
                    requestBody = ByteString.copyFrom("{ }".toByteArray())
                })
            }
        }
    }
}