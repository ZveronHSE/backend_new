package ru.zveron.authservice.persistence.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class MobilePhoneLoginStateContext(
    val phoneNumber: PhoneNumber,
    val code: String? = null,
    val deviceFp: String,
    val codeAttempts: Long = 0,
    val lastAttemptAt: Instant = Instant.now(),
    val isVerified: Boolean = false,
) : StateContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_LOGIN
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class MobilePhoneRegisterStateContext(
    val phoneNumber: PhoneNumber,
    val deviceFp: String,
    val isChannelVerified: Boolean = false,
) : StateContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_REGISTER
}
