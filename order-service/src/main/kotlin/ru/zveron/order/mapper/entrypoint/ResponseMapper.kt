package ru.zveron.order.mapper.entrypoint

import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.fullOrder
import ru.zveron.contract.order.external.getOrderResponse
import ru.zveron.contract.order.external.profile
import ru.zveron.contract.order.model.address
import ru.zveron.contract.order.model.animal
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.util.ChronoFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@Suppress("unused")
fun GetOrderResponseKt.of(response: ru.zveron.order.service.dto.GetOrderResponse) = getOrderResponse {
    this.order = fullOrder {
        id = response.id
        profile = profile {
            id = response.profile.id
            name = response.profile.name
            rating = response.profile.rating.toFloat()
        }
        animal = animal {
            id = response.animal.id
            name = response.animal.name
            breed = response.animal.breed
            species = response.animal.species
            imageUrl = response.animal.imageUrl
        }
        address = address {
            station = response.subwayStation.name
            town = response.subwayStation.town
            color = response.subwayStation.colorHex
        }
        description = response.description
        title = response.title
        serviceDate = """${ChronoFormatter.toDdMmYyyy(response.serviceDateFrom)}${response.serviceDateTo?.let { " - ${ChronoFormatter.toDdMmYyyy(it)}" } ?: ""}"""
        price = """${response.price} ₽"""
        serviceTime = response.timeWindowFrom?.let { from -> "$from${response.timeWindowTo?.let { to -> " - $to" } ?: ""}" }
                ?: ""
        canAccept = Status.canAcceptOrder(response.orderStatus)
        createdAt = if (Instant.now().truncatedTo(ChronoUnit.HOURS) == response.createdAt.truncatedTo(ChronoUnit.HOURS)) {
            "Сегодня в ${
                response.createdAt
                        .let { ZonedDateTime.ofInstant(it, ZoneId.of("Europe/Moscow")) }
                        .let { ChronoFormatter.toHhMm(it) }
            }"
        } else {
            response.createdAt
                    .let { ZonedDateTime.ofInstant(it, ZoneId.of("Europe/Moscow")) }
                    .let { ChronoFormatter.toDdMmYyyy(it) }
        }
    }
}