package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.Profile
import ru.zveron.exception.ProfileException
import ru.zveron.repository.ProfileRepository

@Service
class ProfileService(private val profileRepository: ProfileRepository) {

    fun save(profile: Profile) = profileRepository.save(profile)

    fun deleteById(id: Long) = profileRepository.deleteById(id)

    suspend fun findByIdOrThrow(id: Long): Profile = profileRepository
        .findById(id)
        .orElseThrow { ProfileException("Profile with id: $id doesn't exist") }
}