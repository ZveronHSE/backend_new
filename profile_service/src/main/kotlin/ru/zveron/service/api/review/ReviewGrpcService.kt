package ru.zveron.service.api.review

import org.springframework.stereotype.Service

@Service
class ReviewGrpcService: ReviewService {

    // TODO
    override suspend fun getRating(id: Long): Double = 4.0
}