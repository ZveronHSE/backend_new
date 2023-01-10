package ru.zveron.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.zveron.entity.LotPhoto

@Repository
interface LotPhotoRepository : JpaRepository<LotPhoto, Long>
