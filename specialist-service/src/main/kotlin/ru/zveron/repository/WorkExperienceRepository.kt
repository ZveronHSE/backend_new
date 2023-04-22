package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.WorkExperience

interface WorkExperienceRepository : JpaRepository<WorkExperience, Long>