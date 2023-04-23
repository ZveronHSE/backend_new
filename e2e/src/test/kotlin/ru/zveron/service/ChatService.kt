package ru.zveron.service

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Test
import ru.zveron.E2eTest
import ru.zveron.contract.apigateway.apiGatewayRequest

class ChatService : E2eTest() {

    @Test
    fun `Service response on ping`() {
        assertCallSucceeded(apiGatewayRequest {
            methodAlias = "chatPing"
            requestBody = ByteString.copyFrom("{ }".toByteArray())
        })
    }
}