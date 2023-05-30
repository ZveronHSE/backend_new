package ru.zveron.apigateway.component

import io.grpc.Status
import io.grpc.StatusException
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.marker.Markers.append
import org.springframework.stereotype.Component
import ru.zveron.apigateway.component.constant.ServiceScope
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.exception.ApiGatewayException
import ru.zveron.apigateway.exception.AuthTokenNotValidException
import ru.zveron.apigateway.grpc.client.AccessTokenNotValid
import ru.zveron.apigateway.grpc.client.AccessTokenUnknown
import ru.zveron.apigateway.grpc.client.AccessTokenValid
import ru.zveron.apigateway.grpc.client.GrpcAuthClient

@Component
class AuthResolver(
    private val authClient: GrpcAuthClient,
) {

    companion object : KLogging()

    suspend fun resolveForScope(request: ResolveForRoleRequest): Long? {
        if (request.scope == ServiceScope.ANY) {
            return request.token
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    authClient.verifyAccessToken(request.token).also {
                        logger.debug(
                            append(
                                "response",
                                it,
                            ),
                        ) { "Token not required. Received response from auth-client" }
                    }
                        .let {
                            (it as? AccessTokenValid)?.profileId
                        }
                }
        }

        if (request.scope == ServiceScope.BUYER) {
            if (request.token.isNullOrEmpty()) {
                throw StatusException(Status.DATA_LOSS)
            }

            val authClientResponse = authClient.verifyAccessToken(request.token).also {
                logger.debug("Token required. Received response from auth-client {}", keyValue("response", it))
            }

            when (authClientResponse) {
                is AccessTokenValid -> return authClientResponse.profileId
                is AccessTokenNotValid -> throw AuthTokenNotValidException(metadata = authClientResponse.metadata)
                is AccessTokenUnknown -> throw ApiGatewayException(
                    message = "Unknown auth client error",
                    code = Status.Code.INTERNAL,
                    metadata = authClientResponse.metadata,
                )
            }
        }

        throw ApiGatewayException(message = "Unknown request scope=${request.scope}", Status.Code.INVALID_ARGUMENT)
    }
}
