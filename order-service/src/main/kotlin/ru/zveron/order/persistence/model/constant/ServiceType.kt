package ru.zveron.order.persistence.model.constant

import com.fasterxml.jackson.annotation.JsonCreator
import org.jooq.EnumType

enum class ServiceType : EnumType {
    WALK,
    SITTING,
    BOARDING,
    TRAINING,
    GROOMING,
    OTHER,
    ;


    override fun getLiteral(): String {
        return name
    }

    override fun getName(): String? {
        return "service_type"
    }


    companion object {
        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String): ServiceType {
            return values().singleOrNull { it.name.equals(alias, true) }
                ?: error("Non existent ServiceType for alias=$alias")
        }
    }
}