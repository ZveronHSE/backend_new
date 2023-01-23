package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.LotForm

interface LotFormRepository : JpaRepository<LotForm, Int>