package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Achievement

interface AchievementRepository : JpaRepository<Achievement, Long>