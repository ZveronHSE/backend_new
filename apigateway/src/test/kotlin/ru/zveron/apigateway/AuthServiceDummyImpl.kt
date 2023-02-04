package ru.zveron.apigateway

import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.context.annotation.Primary
import ru.zveron.contract.auth.TestAuthServiceGrpcKt
import ru.zveron.contract.auth.TestRequest
import ru.zveron.contract.auth.TestResponse
import ru.zveron.contract.auth.testResponse

@Primary
@GrpcService
class AuthServiceDummyImpl: TestAuthServiceGrpcKt.TestAuthServiceCoroutineImplBase() {
    override suspend fun testBuyerAccess(request: TestRequest): TestResponse {
        return testResponse {
            this.response = "any response"
        }
    }

    override suspend fun testAnyAccess(request: TestRequest): TestRequest {
        return testResponse {
            this.response = "buyer response"
        }
    }
}