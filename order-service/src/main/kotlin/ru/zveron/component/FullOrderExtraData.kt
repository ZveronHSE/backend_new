package ru.zveron.component

import ru.zveron.service.model.Animal
import ru.zveron.service.model.Profile
import ru.zveron.service.model.SubwayStation

data class FullOrderExtraData(
    val profile: Profile,
    val subwayStation: SubwayStation?,
    val animal: Animal,
)