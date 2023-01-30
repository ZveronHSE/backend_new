package ru.zveron.authservice.persistence.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(value = MobilePhoneLoginStateContext::class, name = MOBILE_PHONE_LOGIN_ALIAS),
    JsonSubTypes.Type(value = MobilePhoneRegisterStateContext::class, name = MOBILE_PHONE_REGISTER_ALIAS),
)
interface StateContext {
    val type: ContextType
}
