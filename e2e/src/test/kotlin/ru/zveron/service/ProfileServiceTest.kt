package ru.zveron.service

import com.google.protobuf.ByteString
import org.junit.jupiter.api.Test
import ru.zveron.E2eTest
import ru.zveron.contract.apigateway.apiGatewayRequest

class ProfileServiceTest : E2eTest() {

    @Test
    fun `Service respond on profileGetPage`() {
        assertCallSucceeded(apiGatewayRequest {
            methodAlias = "profileGetPage"
            requestBody = ByteString.copyFrom(getProfilePageRequest())
        })
    }

    private fun getProfilePageRequest() = """
        {
            "requested_profile_id": "2"
        }
    """.trimIndent().toByteArray()
}