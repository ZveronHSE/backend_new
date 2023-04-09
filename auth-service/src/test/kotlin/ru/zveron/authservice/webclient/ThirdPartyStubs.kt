package ru.zveron.authservice.webclient

import com.github.tomakehurst.wiremock.client.WireMock
import org.springframework.http.HttpStatus
import ru.zveron.authservice.util.withJsonDto

object ThirdPartyStubs {
    fun <T : Any> serverStubForUserGetInfo(result: T, path: String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*$path"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withJsonDto(result)
                )
        )
    }

    fun serverStubUserInfoFail(status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR, path: String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*$path"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(status.value())
                )
        )
    }
}