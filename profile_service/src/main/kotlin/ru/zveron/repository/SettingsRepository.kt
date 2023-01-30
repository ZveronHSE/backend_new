package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Settings

interface SettingsRepository : JpaRepository<Settings, Long>
