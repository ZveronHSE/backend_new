package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Service

interface ServiceRepository : JpaRepository<Service, Long>