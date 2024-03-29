package ru.zveron.authservice.webclient.notifier

import io.grpc.Status
import org.springframework.web.reactive.function.client.WebClient
import ru.zveron.authservice.webclient.notifier.model.GetVerificationCodeRequest

class NotifierClient(
    private val client: WebClient,
) {

    fun initializeVerification(req: GetVerificationCodeRequest): NotifierResponse =
        NotifierSuccess("1111")
}

sealed class NotifierResponse

data class NotifierFailure(val code: Status.Code, val message: String?) : NotifierResponse()

data class NotifierSuccess(val verificationCode: String) : NotifierResponse()
