package ru.zveron.apigateway.controller

import kotlinx.coroutines.flow.toList
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.zveron.apigateway.persistence.entity.MethodMetadata
import ru.zveron.apigateway.persistence.repository.MethodMetadataRepository

@RestController
@RequestMapping("/api/v1")
class MetadataController(
    private val metadataRepository: MethodMetadataRepository,
) {

    @PostMapping("/method/alias/get")
    suspend fun getAllAliases(): List<MethodMetadata> {
        return metadataRepository.findAll().toList()
    }

    @PostMapping("/method/alias/upsert")
    suspend fun upsertAlias(
        @RequestBody methodMetadata: MethodMetadata,
    ) {
        metadataRepository.save(methodMetadata)
    }
}
