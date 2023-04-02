package ru.zveron.authservice.webclient.thirdparty

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.logstash.logback.marker.Markers.append
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoFailure
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoResponse
import ru.zveron.authservice.webclient.thirdparty.model.GetThirdPartyUserInfoSuccess
import java.net.URI

class ThirdPartyClient(
    private val webClient: WebClient,
    private val jsonObjectMapper: ObjectMapper,
) {

    companion object : KLogging()

    suspend fun <T> getUserInfo(uri: URI, responseClass: Class<T>): GetThirdPartyUserInfoResponse<T> {
        return try {
            val response = executeGetUserInfo(uri, responseClass)
            GetThirdPartyUserInfoSuccess(response = response)
        } catch (ex: WebClientResponseException) {
            logger.error(ex) { "Failed to get user info" }
            GetThirdPartyUserInfoFailure(code = ex.statusCode, errorMessage = ex.message)
        } catch (ex: Exception) {
            logger.error(ex) { "Failed to get user info" }
            GetThirdPartyUserInfoFailure(code = HttpStatus.INTERNAL_SERVER_ERROR, errorMessage = ex.message)
        }
    }

    private suspend fun <T> executeGetUserInfo(uri: URI, responseClass: Class<T>): T {
        return webClient.get()
            .uri(uri)
            .retrieve()
            .awaitBody<String>()
            .also { logger.debug(append("response", it)) { "Get user info client response" } }
            .let {
                jsonObjectMapper.readValue(it, responseClass)
            }
    }
}
