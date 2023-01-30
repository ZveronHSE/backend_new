package ru.zveron.service.client.review

import org.springframework.stereotype.Service

@Service
interface ReviewClient {

    suspend fun getRating(id: Long): Double
}
