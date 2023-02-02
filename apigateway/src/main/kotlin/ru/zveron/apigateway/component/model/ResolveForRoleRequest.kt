package ru.zveron.apigateway.component.model

data class ResolveForRoleRequest(
    val role: ServiceRole,
    val token: String,
)
