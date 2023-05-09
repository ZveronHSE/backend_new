package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Education

interface EducationRepository : JpaRepository<Education, Long>