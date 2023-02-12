package ru.zveron.apigateway.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import ru.zveron.apigateway.persistence.constant.AccessScope

@Table("method_metadata")
data class MethodMetadata(
    @Id
    val alias: String? = null,

    val serviceName: String,
    val grpcServiceName: String,
    val grpcMethodName: String,

    val accessScope: AccessScope,
)
