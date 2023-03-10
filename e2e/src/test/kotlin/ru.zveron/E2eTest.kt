package ru.zveron

import io.grpc.ManagedChannelBuilder
import io.grpc.StatusException
import io.kotest.assertions.retry
import io.kotest.assertions.throwables.shouldNotThrow
import kotlinx.coroutines.runBlocking
import ru.zveron.contract.apigateway.ApiGatewayRequest
import ru.zveron.contract.apigateway.ApigatewayServiceGrpcKt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val DEFAULT_RETRIES_NUMBER = 5
private const val DEFAULT_RETRY_DELAY_MINUTES = 2

abstract class E2eTest {

    private val client = ApigatewayServiceGrpcKt.ApigatewayServiceCoroutineStub(
        ManagedChannelBuilder.forAddress("zveron.ru", 80).usePlaintext().build()
    )

    // System.getenv

    protected fun assertCallSucceeded(
        apiGatewayRequest: ApiGatewayRequest,
        maxRetry: Int = DEFAULT_RETRIES_NUMBER,
        delayMinutes: Int = DEFAULT_RETRY_DELAY_MINUTES
    ) {
        runBlocking {
            retry(maxRetry, Duration.INFINITE, delayMinutes.minutes) {
                shouldNotThrow<StatusException> {
                    client.callApiGateway(apiGatewayRequest)
                }
            }
        }
    }
}