package ru.zveron.test.util

import io.kotest.matchers.shouldBe
import ru.zveron.persistence.entity.OrderLot
import ru.zveron.persistence.repository.model.OrderLotWrapper

infix fun OrderLotWrapper.shouldBeOrderLot(o: OrderLot) {
    this.id shouldBe o.id
    this.subwayId shouldBe o.subwayId
    this.animalId shouldBe o.animalId
    this.price shouldBe o.price
    this.createdAt.toEpochMilli() shouldBe o.createdAt.toEpochMilli()
    this.serviceDateFrom shouldBe o.serviceDateFrom
    this.serviceDateTo shouldBe o.serviceDateTo
    this.title shouldBe o.title
}
