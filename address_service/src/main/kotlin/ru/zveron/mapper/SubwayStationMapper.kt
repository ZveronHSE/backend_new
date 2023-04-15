package ru.zveron.mapper

import ru.zveron.contract.address.external.ExtSubwayStationKt
import ru.zveron.contract.address.external.extSubwayStation
import ru.zveron.contract.address.internal.IntSubwayStationKt
import ru.zveron.contract.address.internal.intSubwayStation
import ru.zveron.entity.SubwayStation

object SubwayStationMapper {

    fun ExtSubwayStationKt.ofEntity(s: SubwayStation) = extSubwayStation {
        this.id = s.id
        this.name = s.name
        this.colorHex = s.colorHex
    }

    fun IntSubwayStationKt.ofEntity(s: SubwayStation) = intSubwayStation {
        this.id = s.id
        this.name = s.name
        this.colorHex = s.colorHex
    }
}
