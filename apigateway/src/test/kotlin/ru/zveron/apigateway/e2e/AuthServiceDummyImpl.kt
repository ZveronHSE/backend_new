package ru.zveron.apigateway.e2e

import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.context.annotation.Primary
import ru.zveron.contract.auth.TestAuthServiceGrpcKt
import ru.zveron.contract.auth.TestRequest
import ru.zveron.contract.auth.TestResponse
import ru.zveron.contract.auth.testResponse

@Primary
@GrpcService
class AuthServiceDummyImpl : TestAuthServiceGrpcKt.TestAuthServiceCoroutineImplBase() {
    override suspend fun testBuyerAccess(request: TestRequest): TestResponse {
        if (request.profileId == 123L) {
            return testResponse {
                this.response = "any response"
            }
        }

        throw Exception("No profile_id in request")
    }

    override suspend fun testAnyAccess(request: TestRequest): TestResponse {
        return testResponse {
            this.response = "buyer response"
        }
    }
}