package ru.zveron.apigateway.grpc.client

import com.google.protobuf.Empty
import io.grpc.Metadata
import io.grpc.Status.Code
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import ru.zveron.contract.auth.internal.AuthServiceInternalGrpcKt

class GrpcAuthClient(
    private val client: AuthServiceInternalGrpcKt.AuthServiceInternalCoroutineStub
) {

    companion object : KLogging() {
        val accessTokenKey = Metadata.Key.of("access_token", Metadata.ASCII_STRING_MARSHALLER)
    }

    suspend fun verifyAccessToken(token: String?): GrpcAuthClientResponse {
        return try {
            val response = client.verifyToken(
                request = Empty.getDefaultInstance(),
                headers = Metadata().apply {
                    this.put(accessTokenKey, token)
                }
            )

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

