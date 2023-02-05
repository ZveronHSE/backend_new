package ru.zveron.apigateway.component.model

data class ResolveForRoleRequest(
    val scope: ServiceScope,
    val token: String?,
)
