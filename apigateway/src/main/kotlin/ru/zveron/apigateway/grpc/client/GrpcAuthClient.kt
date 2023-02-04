package ru.zveron.apigateway.grpc.client

import io.grpc.Metadata
import io.grpc.Status.Code
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.auth.AuthServiceGrpcKt
import ru.zveron.contract.auth.verifyMobileTokenRequest

class GrpcAuthClient(
    private val client: AuthServiceGrpcKt.AuthServiceCoroutineStub
) {

    companion object : KLogging()

    suspend fun verifyAccessToken(token: String): GrpcAuthClientResponse {
        return try {
            client.verifyToken(verifyMobileTokenRequest {
                this.accessToken = token
            })
            AccessTokenValid
        } catch (ex: StatusException) {
            logger.error(append("status", ex.status)) { "Auth client request failed" }
            when (val code = ex.status.code) {
                Code.UNAUTHENTICATED -> AccessTokenNotValid(message = ex.message, code = code, metadata = ex.trailers)
                else -> AccessTokenUnknown(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }
}

sealed class GrpcAuthClientResponse

object AccessTokenValid : GrpcAuthClientResponse()

data class AccessTokenNotValid(
    val message: String?,
    val code: Code,
    val metadata: Metadata,
) : GrpcAuthClientResponse()

data class AccessTokenUnknown(
    val message: String?,
    val code: Code,
    val metadata: Metadata,
) : GrpcAuthClientResponse()
