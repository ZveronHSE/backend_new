package ru.zveron.order.entrpoint.mapper

import ru.zveron.contract.order.external.*
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
            response.subwayStation?.let { address = AddressKt.of(it) }

            description = response.description
            title = response.title
            price = PriceFormatter.formatToPrice(response.price)

            serviceDate = ChronoFormatter.formatServiceDate(response.serviceDateFrom, response.serviceDateTo)
            serviceTime = ChronoFormatter.formatServiceTime(response.timeWindowFrom, response.timeWindowTo)
            createdAt = ChronoFormatter.formatCreatedAt(response.createdAt)

            canAccept = Status.canAcceptOrder(response.orderStatus)
        }
    }

    fun GetWaterfallResponseKt.of(waterfallOrderLots: List<WaterfallOrderLot>) = getWaterfallResponse {
        this.orders.addAll(
            waterfallOrderLots.map { WaterfallOrderKt.of(it) }
        )
    }

    fun GetCustomerResponseKt.of(serviceResponse: ru.zveron.order.service.model.GetCustomerResponse): GetCustomerResponse {
        return getCustomerResponse {
            this.customer = customer {
                this.id = serviceResponse.profile.id
                this.name = serviceResponse.profile.name
                this.rating = serviceResponse.profile.rating.toFloat()
                this.imageUrl = serviceResponse.profile.imageUrl
                this.activeOrders.addAll(serviceResponse.orderLots.filter { Status.canAcceptOrder(it.status) }
                    .map { CustomerActiveOrderKt.of(it) })
                this.completedOrders.addAll(serviceResponse.orderLots.filter { it.status == Status.COMPLETED }
                    .map { CustomerCompletedOrderKt.of(it) })
            }
        }
    }

    fun CustomerActiveOrderKt.of(serviceResponse: ru.zveron.order.service.model.CustomerProfileOrder): CustomerActiveOrder {
        return customerActiveOrder {
            this.id = serviceResponse.id
            this.animal = AnimalKt.of(serviceResponse.animal)
            this.price = PriceFormatter.formatToPrice(serviceResponse.price)
            this.title = serviceResponse.title
            this.createdAt = ChronoFormatter.formatCreatedAt(serviceResponse.createdAt)
            serviceResponse.subway?.let { this.address = AddressKt.of(it) }
            this.serviceDate =
                ChronoFormatter.formatServiceDate(serviceResponse.serviceDateFrom, serviceResponse.serviceDateTo)
        }
    }

    fun CustomerCompletedOrderKt.of(serviceResponse: ru.zveron.order.service.model.CustomerProfileOrder): CustomerCompletedOrder {
        return customerCompletedOrder {
            this.id = serviceResponse.id
            this.animal = AnimalKt.of(serviceResponse.animal)
            this.price = PriceFormatter.formatToPrice(serviceResponse.price)
            this.title = serviceResponse.title
            this.createdAt = ChronoFormatter.formatCreatedAt(serviceResponse.createdAt)
            serviceResponse.subway?.let { this.address = AddressKt.of(it) }
            this.serviceDate =
                ChronoFormatter.formatServiceDate(serviceResponse.serviceDateFrom, serviceResponse.serviceDateTo)
        }
    }
}
