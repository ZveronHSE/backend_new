package ru.zveron.authservice.webclient

import io.grpc.Status
import org.springframework.web.reactive.function.client.WebClient
import ru.zveron.authservice.webclient.dto.GetVerificationCodeRequest
import java.util.concurrent.atomic.AtomicInteger

class NotifierClient(
    private val client: WebClient,
) {

    private val shift = AtomicInteger()

    fun initializeVerification(req: GetVerificationCodeRequest): NotifierResponse =
        NotifierSuccess(req.phoneNumber.takeLast(3) + (shift.incrementAndGet() % 2).toString())
}

sealed class NotifierResponse

data class NotifierFailure(val code: Status.Code, val message: String?) : NotifierResponse()

data class NotifierSuccess(val verificationCode: String) : NotifierResponse()
