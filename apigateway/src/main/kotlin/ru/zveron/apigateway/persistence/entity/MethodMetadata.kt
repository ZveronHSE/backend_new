package ru.zveron.apigateway.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import ru.zveron.apigateway.component.model.ServiceRole

@Table("method_metadata")
data class MethodMetadata(
    @Id
    val alias: String? = null,

    val serviceName: String,
    val grpcServiceName: String,
    val grpcMethodName: String,

    val accessRole: AccessRole,
)

enum class AccessRole {
    ANY,
    BUYER,
}

fun AccessRole.toServiceRole() = when {
    this == AccessRole.ANY -> ServiceRole.ANY
    this == AccessRole.BUYER -> ServiceRole.BUYER
    else -> error("Unknown access role type")
}
