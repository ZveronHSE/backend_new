package ru.zveron.service.model

data class GetCustomerResponse(
    val profile: Profile,
    val orderLots: List<CustomerProfileOrder>,
)

