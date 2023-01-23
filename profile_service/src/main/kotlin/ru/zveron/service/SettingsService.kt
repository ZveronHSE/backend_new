package ru.zveron.service

import org.springframework.stereotype.Service
import ru.zveron.entity.Settings
import ru.zveron.exception.ProfileException
import ru.zveron.repository.SettingsRepository

@Service
class SettingsService(private val repository: SettingsRepository) {

    fun save(settings: Settings) = repository.save(settings)

    fun findByIdOrThrow(id: Long) =
        repository.findById(id).orElseThrow { ProfileException("Profile with id: $id doesn't exist") }
}