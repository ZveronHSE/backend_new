package ru.zveron.apigateway.component.model

import ru.zveron.apigateway.component.constant.ServiceScope

data class ResolveForRoleRequest(
    val scope: ServiceScope,
    val token: String?,
)
