package ru.zveron.apigateway.persistence.entity

import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class MethodMetadata(
    @Id
    val alias: String? = null,
    val serviceName: String,
    val grpcServiceName: String,
    val grpcMethodName: String,
)
