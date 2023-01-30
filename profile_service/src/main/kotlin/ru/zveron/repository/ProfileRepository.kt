package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Profile

interface ProfileRepository : JpaRepository<Profile, Long>
