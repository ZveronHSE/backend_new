package ru.zveron.persistence.model.constant

import com.fasterxml.jackson.annotation.JsonCreator

enum class ServiceType {
    WALK,
    SITTING,
    BOARDING,
    TRAINING,
    GROOMING,
    OTHER,
    ;


    companion object {
        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String): ServiceType {
            return values().singleOrNull { it.name.equals(alias, true) }
                ?: error("Non existent ServiceType for alias=$alias")
        }
    }
}
