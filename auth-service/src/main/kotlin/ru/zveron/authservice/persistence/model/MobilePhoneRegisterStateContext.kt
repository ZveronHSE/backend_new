package ru.zveron.authservice.persistence.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MobilePhoneRegisterStateContext(
    val phoneNumber: PhoneNumber,
    val fingerprint: String,
    val isChannelVerified: Boolean = false,
) : StateContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_REGISTER
}
