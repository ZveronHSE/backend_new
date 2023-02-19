package ru.zveron.apigateway.persistence.constant

import com.fasterxml.jackson.annotation.JsonCreator

enum class AccessScope {
    ANY,
    BUYER,
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String) {
            AccessScope.values().singleOrNull { it.name.equals(alias, true) }
                ?: error("Non existent ContextType for alias=$alias")
        }
    }
}
