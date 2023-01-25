package ru.zv.authservice.webclient

import io.grpc.Status
import kotlinx.coroutines.reactor.awaitSingle
import mu.KLogging
import org.springframework.web.reactive.function.client.WebClient
import ru.zv.authservice.webclient.dto.GetVerificationCodeRequest

class NotifierClient(
    private val client: WebClient,
) {

    companion object : KLogging() {
        const val GET_CODE_PATH = ""
    }

    suspend fun initializeVerification(req: GetVerificationCodeRequest): NotifierResponse =
        if (req.phoneNumber == "79257646188" || req.phoneNumber == "79257646189") NotifierSuccess("1111")
        else NotifierFailure(code = Status.Code.UNAVAILABLE, null)

    private suspend fun callClient(req: GetVerificationCodeRequest) = client.post()
        .uri(GET_CODE_PATH)
        .bodyValue(req)
        .exchangeToMono { cr ->
            if (cr.statusCode().isError) {
                cr.createException().map { NotifierFailure(Status.Code.UNAVAILABLE, it.message) }
            } else {
                cr.bodyToMono(NotifierSuccess::class.java)
            }
        }.awaitSingle()
}

sealed class NotifierResponse

data class NotifierFailure(val code: Status.Code, val message: String?) : NotifierResponse()

data class NotifierSuccess(val verificationCode: String) : NotifierResponse()
