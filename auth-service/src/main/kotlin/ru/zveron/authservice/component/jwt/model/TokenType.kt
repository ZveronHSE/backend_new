package ru.zveron.authservice.component.jwt.model

import com.fasterxml.jackson.annotation.JsonCreator

enum class TokenType {
    ACCESS,
    REFRESH,
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromName(name: String) =
            values().singleOrNull { it.name.equals(name, true) } ?: error("Unknown token type provided $name")
    }
}