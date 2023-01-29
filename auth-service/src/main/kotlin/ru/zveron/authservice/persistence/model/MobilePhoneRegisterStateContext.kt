package ru.zveron.authservice.persistence.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MobilePhoneRegisterStateContext(
    val phoneNumber: PhoneNumber,
    val deviceFp: String,
    val isChannelVerified: Boolean = false,
) : StateContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_REGISTER
}