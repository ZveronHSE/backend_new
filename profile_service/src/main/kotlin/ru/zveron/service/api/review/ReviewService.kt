package ru.zveron.service.api.review

import org.springframework.stereotype.Service

@Service
interface ReviewService {

    suspend fun getRating(id: Long): Double
}