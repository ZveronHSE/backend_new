package ru.zveron.order.component

import ru.zveron.order.service.model.Animal
import ru.zveron.order.service.model.Profile
import ru.zveron.order.service.model.SubwayStation

data class FullOrderExtraData(
    val profile: Profile,
    val subwayStation: SubwayStation?,
    val animal: Animal,
)