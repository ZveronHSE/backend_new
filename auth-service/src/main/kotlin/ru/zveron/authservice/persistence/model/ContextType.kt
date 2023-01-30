package ru.zveron.authservice.persistence.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

const val MOBILE_PHONE_LOGIN_ALIAS = "mobile-phone-login"
const val MOBILE_PHONE_REGISTER_ALIAS = "mobile-phone-register"

enum class ContextType(
    @field:JsonValue
    val alias: String,
) {
    MOBILE_PHONE_LOGIN(MOBILE_PHONE_LOGIN_ALIAS),
    MOBILE_PHONE_REGISTER(MOBILE_PHONE_REGISTER_ALIAS),
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String) {
            ContextType.values().singleOrNull { it.alias.equals(alias, true) }
                ?: error("Non existent ContextType for alias=$alias")
        }
    }
}
