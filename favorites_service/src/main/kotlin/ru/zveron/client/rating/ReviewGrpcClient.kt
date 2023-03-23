package ru.zveron.client.rating

import org.springframework.stereotype.Service

@Service
class ReviewGrpcClient: ReviewClient {

    override suspend fun getProfileRating(profileId: Long): Double = 4.2
}