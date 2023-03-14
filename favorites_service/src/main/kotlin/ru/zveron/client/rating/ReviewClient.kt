package ru.zveron.client.rating

interface ReviewClient {

    suspend fun getProfileRating(profileId: Long): Double
}