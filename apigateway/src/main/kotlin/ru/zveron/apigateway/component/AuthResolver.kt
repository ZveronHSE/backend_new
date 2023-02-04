package ru.zveron.apigateway.component

import io.grpc.Status
import io.grpc.StatusException
import org.springframework.stereotype.Component
import ru.zveron.apigateway.component.model.ResolveForRoleRequest
import ru.zveron.apigateway.component.model.ServiceRole
import ru.zveron.apigateway.grpc.client.AccessTokenNotValid
import ru.zveron.apigateway.grpc.client.AccessTokenUnknown
import ru.zveron.apigateway.grpc.client.AccessTokenValid
import ru.zveron.apigateway.grpc.client.GrpcAuthClient

@Component
class AuthResolver(
    private val authClient: GrpcAuthClient,
) {

    suspend fun resolveForRole(request: ResolveForRoleRequest) {
        if (request.role == ServiceRole.ANY) {
            return
        }

        if (request.token.isEmpty()) {
            throw StatusException(Status.DATA_LOSS)
        }

        val authClientResponse = authClient.verifyAccessToken(request.token)
        when (authClientResponse) {
            is AccessTokenValid -> return
            is AccessTokenNotValid -> throw StatusException(Status.UNAUTHENTICATED, authClientResponse.metadata)
            is AccessTokenUnknown -> throw StatusException(Status.INTERNAL, authClientResponse.metadata)
        }
    }
}