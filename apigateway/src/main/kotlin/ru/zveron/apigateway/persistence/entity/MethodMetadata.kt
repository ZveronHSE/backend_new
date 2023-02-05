package ru.zveron.apigateway.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("method_metadata")
data class MethodMetadata(
    @Id
    val alias: String? = null,

    val serviceName: String,
    val grpcServiceName: String,
    val grpcMethodName: String,

    val accessRole: AccessRole,
)

