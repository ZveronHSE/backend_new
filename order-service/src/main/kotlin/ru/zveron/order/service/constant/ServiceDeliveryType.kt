package ru.zveron.order.service.constant

enum class ServiceDeliveryType {
    IN_PERSON,
    REMOTE,
    ;

    companion object {

        @JvmStatic
        fun ofName(name: String) = values().firstOrNull { it.name.equals(name, true) }
            ?: throw IllegalStateException("Unknown service delivery type with type=$name")
    }
}