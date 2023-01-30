package ru.zveron.service.client.review

import org.springframework.stereotype.Service

@Service
class ReviewGrpcClient : ReviewClient {

    // TODO
    override suspend fun getRating(id: Long): Double = 4.0
}
