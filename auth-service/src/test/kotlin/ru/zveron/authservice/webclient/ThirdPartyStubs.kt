package ru.zveron.authservice.webclient

import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import ru.zveron.authservice.component.thirdparty.GoogleProvider
import ru.zveron.authservice.util.withJsonDto

object ThirdPartyStubs {
    fun <T : Any> serverStubForGoogleGetUserInfo(result: T) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*${GoogleProvider.USERS_GET_PATH}"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withJsonDto(result)
                )
        )
    }

    fun serverStubForGoogleGetUserInfoFail(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*${GoogleProvider.USERS_GET_PATH}"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status.value())
                )
        )
    }
}