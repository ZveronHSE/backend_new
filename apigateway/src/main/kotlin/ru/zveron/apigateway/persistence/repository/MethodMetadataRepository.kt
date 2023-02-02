package ru.zveron.apigateway.persistence.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import ru.zveron.apigateway.persistence.entity.MethodMetadata

interface MethodMetadataRepository : CoroutineCrudRepository<MethodMetadata, String?> {
    suspend fun findByAlias(alias: String): MethodMetadata?
}
