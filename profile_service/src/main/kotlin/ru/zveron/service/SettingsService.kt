package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.Settings
import ru.zveron.exception.ProfileNotFoundException
import ru.zveron.repository.SettingsRepository

@Service
class SettingsService(private val repository: SettingsRepository) {

    fun save(settings: Settings) = repository.save(settings)

    fun findByIdOrThrow(id: Long) =
        repository.findById(id).orElseThrow { ProfileNotFoundException("Profile with id: $id doesn't exist") }
}