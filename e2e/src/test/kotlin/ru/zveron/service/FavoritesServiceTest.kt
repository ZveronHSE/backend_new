package ru.zveron.service

import com.google.protobuf.ByteString
import io.grpc.StatusException
import io.kotest.assertions.throwables.shouldNotThrow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import ru.zveron.E2eTest
import ru.zveron.contract.apigateway.apiGatewayRequest

@Disabled("No test tokens yet")
class FavoritesServiceTest: E2eTest() {

    @Test
    fun `Service respond on profileFavoriteGet`() {
        shouldNotThrow<StatusException> {
            runBlocking {
                assertCallSucceeded(apiGatewayRequest {
                    methodAlias = "profileFavoriteGet"
                    requestBody = ByteString.copyFrom("{ }".toByteArray())
                })
            }
        }
    }
}