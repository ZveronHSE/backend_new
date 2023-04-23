package ru.zveron.order.persistence.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import ru.zveron.order.persistence.model.constant.ServiceDeliveryType
import ru.zveron.order.persistence.model.constant.ServiceType
import ru.zveron.order.persistence.model.constant.Status
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Table("order_lot")
data class OrderLot(
        @Id
        val id: Long? = null,
        //customer profile id
        val profileId: Long,
        val animalId: Long,
        val subwayId: Int,

        val price: String,
        val title: String,
        val description: String,

        val serviceDateFrom: LocalDate,
        val serviceDateTo: LocalDate?,

        val timeWindowFrom: LocalTime?,
        val timeWindowTo: LocalTime?,

        val status: Status,
        val type: ServiceType,
        val serviceDeliveryType: ServiceDeliveryType,

        @CreatedDate
        val createdAt: Instant = Instant.now(),
)

