package ru.zveron.apigateway.persistence.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.apigateway.persistence.entity.MethodMetadata

interface MethodMetadataRepository : JpaRepository<MethodMetadata, String?> {
    fun findByAlias(alias: String): MethodMetadata?
}
