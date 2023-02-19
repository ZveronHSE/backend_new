package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.Parameter

interface ParameterRepository : JpaRepository<Parameter, Int>