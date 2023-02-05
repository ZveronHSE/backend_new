package ru.zveron.apigateway.grpc.client

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
            val response = client.verifyToken(verifyMobileTokenRequest { accessToken = token })
            AccessTokenValid(response.id)
        } catch (ex: StatusException) {
            logger.error(append("status", ex.status)) { "Auth client request failed" }

            when (val code = ex.status.code) {
                Code.UNAUTHENTICATED -> AccessTokenNotValid(message = ex.message, code = code, metadata = ex.trailers)
                else -> AccessTokenUnknown(message = ex.message, code = code, metadata = ex.trailers)
            }
        }
    }
}

