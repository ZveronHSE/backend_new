package ru.zv.authservice.persistence.model

import java.time.Instant

data class MobilePhoneLoginFlowContext(
    val phoneNumber: PhoneNumber,
    val code: String? = null,
    val deviceFp: String,
    val codeAttempts: Int = 0,
    val lastAttemptAt: Instant = Instant.now(),
    val isVerified: Boolean = false,
) : FlowContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_LOGIN
}

data class MobilePhoneRegisterFlowContext(
    val phoneNumber: PhoneNumber,
    val deviceFp: String,
    val isChannelVerified: Boolean = false,
) : FlowContext {
    override val type: ContextType = ContextType.MOBILE_PHONE_REGISTER
}
