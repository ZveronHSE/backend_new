package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.zveron.entity.OtherInfo

interface OtherInfoRepository : JpaRepository<OtherInfo, Long>