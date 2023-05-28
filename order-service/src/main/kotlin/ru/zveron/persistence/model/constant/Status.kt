package ru.zveron.persistence.model.constant

import com.fasterxml.jackson.annotation.JsonCreator

enum class Status {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    UPDATING,
    ;

    companion object {

        fun canAcceptOrder(status: Status) = !(status == CANCELLED || status == COMPLETED)

        fun terminalStatuses() = listOf(CANCELLED, COMPLETED)

        @JvmStatic
        @JsonCreator
        fun byAlias(alias: String) {
            values().singleOrNull { it.name.equals(alias, true) }
                ?: error("Non existent Status for alias=$alias")
        }
    }
}
