package ru.zveron.order.persistence.model.constant

import com.fasterxml.jackson.annotation.JsonCreator
import org.jooq.EnumType

enum class ServiceType(val alias: String) : EnumType {
    WALK("WALK"),
    SITTING("SITTING"),
    BOARDING("BOARDING"),
    TRAINING("TRAINING"),
    GROOMING("GROOMING"),
    OTHER("OTHER"),
    ;


    override fun getLiteral(): String {
        return alias
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