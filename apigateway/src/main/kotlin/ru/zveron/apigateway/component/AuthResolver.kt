package ru.zveron.apigateway.component

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Component
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.component.model.ServiceScope
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

    suspend fun resolveForScope(request: ResolveForRoleRequest): Long? {
        if (request.scope == ServiceScope.ANY) {
            return null
        }

        if (request.token.isNullOrEmpty()) {
            throw StatusException(Status.DATA_LOSS)
        }

        return verifyTokenAndGetId(request.token)
    }

    private suspend fun verifyTokenAndGetId(token: String): Long {
        val authClientResponse = authClient.verifyAccessToken(token)
        when (authClientResponse) {
            is AccessTokenValid -> return authClientResponse.profileId
            is AccessTokenNotValid -> throw AuthTokenNotValidException(metadata = authClientResponse.metadata)
            is AccessTokenUnknown -> throw ApiGatewayException(
                message = "Unknown auth client error",
                code = Status.Code.INTERNAL,
                metadata = authClientResponse.metadata
            )
        }
    }
}