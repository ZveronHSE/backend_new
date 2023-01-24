package ru.zv.authservice.persistence.model

import java.time.Instant

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

data class MobilePhoneRegisterStateContext(
    val phoneNumber: PhoneNumber,
    val deviceFp: String,
    val isChannelVerified: Boolean = false,
) : StateContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_REGISTER
}
