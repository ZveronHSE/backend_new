package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.GetWaterfallResponseKt
import ru.zveron.contract.order.external.ProfileKt
import ru.zveron.contract.order.external.WaterfallOrderKt
import ru.zveron.contract.order.external.fullOrder
import ru.zveron.contract.order.external.getOrderResponse
import ru.zveron.contract.order.external.getWaterfallResponse
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.order.entrpoint.mapper.CommonDtoMapper.of
import ru.zveron.order.persistence.model.constant.Status
import ru.zveron.order.service.model.WaterfallOrderLot
import ru.zveron.order.util.ChronoFormatter
import ru.zveron.order.util.PriceFormatter

@Suppress("unused")
object ResponseMapper {
    fun GetOrderResponseKt.of(response: ru.zveron.order.service.model.GetOrderResponse) = getOrderResponse {
        this.order = fullOrder {
            id = response.id
            profile = ProfileKt.of(response.profile)
            animal = AnimalKt.of(response.animal)
            address = AddressKt.of(response.subwayStation)
            description = response.description
            title = response.title
            serviceDate = ChronoFormatter.formatServiceDate(response.serviceDateFrom, response.serviceDateTo)
            price = PriceFormatter.formatToPrice(response.price)
            serviceTime = ChronoFormatter.formatServiceTime(response.timeWindowFrom, response.timeWindowTo)
            canAccept = Status.canAcceptOrder(response.orderStatus)
            createdAt = ChronoFormatter.formatCreatedAt(response.createdAt)
        }
    }

    fun GetWaterfallResponseKt.of(waterfallOrderLots: List<WaterfallOrderLot>) = getWaterfallResponse {
        this.orders.addAll(
            waterfallOrderLots.map { WaterfallOrderKt.of(it) }
        )
    }
}
