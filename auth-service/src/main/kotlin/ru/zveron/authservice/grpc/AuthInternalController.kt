package ru.zveron.authservice.grpc

import com.google.protobuf.Empty
import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.authservice.component.auth.Authenticator
import ru.zveron.authservice.grpc.context.AccessTokenElement
import ru.zveron.contract.auth.internal.AuthServiceInternalGrpcKt
import ru.zveron.contract.auth.internal.ProfileDto
import ru.zveron.contract.auth.internal.profileDto
import kotlin.coroutines.coroutineContext

@GrpcService
class AuthInternalController(
    private val authenticator: Authenticator,
) : AuthServiceInternalGrpcKt.AuthServiceInternalCoroutineImplBase() {

    override suspend fun verifyToken(request: Empty): ProfileDto {
        val accessToken = coroutineContext[AccessTokenElement.Key]?.accessToken
        val profileId = authenticator.validateAccessToken(accessToken)
        return profileDto {
            this.id = profileId
        }
    }
}
