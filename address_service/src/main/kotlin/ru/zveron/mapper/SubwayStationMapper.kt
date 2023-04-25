package ru.zveron.mapper

import ru.zveron.contract.address.external.SubwayStationExtKt
import ru.zveron.contract.address.external.subwayStationExt
import ru.zveron.contract.address.internal.SubwayStationIntKt
import ru.zveron.contract.address.internal.subwayStationInt
import ru.zveron.entity.SubwayStation

object SubwayStationMapper {

    fun SubwayStationIntKt.ofEntity(s: SubwayStation) = subwayStationInt {
        this.id = s.id
        this.name = s.name
        this.colorHex = s.colorHex
        this.town = s.city
    }

    fun SubwayStationExtKt.ofEntity(s: SubwayStation) = subwayStationExt {
        this.id = s.id
        this.name = s.name
        this.colorHex = s.colorHex
    }
}
