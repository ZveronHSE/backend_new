package ru.zveron.persistence.model.constant

import com.fasterxml.jackson.annotation.JsonCreator

enum class ServiceDeliveryType {
    REMOTE,
    IN_PERSON,
    ;

    companion object {
        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String): ServiceDeliveryType = values().singleOrNull { it.name.equals(alias, true) }
            ?: error("Non existent ServiceDeliveryType for alias=$alias")
    }
}
