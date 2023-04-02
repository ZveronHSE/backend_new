package ru.zveron.authservice.exception

import io.grpc.Status
import io.grpc.internal.GrpcUtil
import org.springframework.http.HttpStatus
import ru.zveron.authservice.webclient.thirdparty.model.ResponseFailure

class SocialMediaException(
    message: String? = "Social media request failed",
    code: Status.Code = Status.Code.INTERNAL,
) : AuthException(message, code) {
    companion object Companion {
        fun of(failedResponse: ResponseFailure) = SocialMediaException(
            failedResponse.getMessage(),
            GrpcUtil.httpStatusToGrpcStatus(
                failedResponse.getHttpStatusCode()?.value() ?: HttpStatus.INTERNAL_SERVER_ERROR.value()
            ).code
        )
    }
}