package ru.zveron.authservice.component.jwt.model

import ru.zveron.authservice.persistence.entity.SessionEntity

data class IssueMobileTokensRequest(
    val profileId: Long,
    val session: SessionEntity,
)
