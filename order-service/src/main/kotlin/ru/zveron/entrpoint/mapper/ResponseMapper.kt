package ru.zveron.entrpoint.mapper

import ru.zveron.contract.order.external.CreateOrderResponseKt
import ru.zveron.contract.order.external.CustomerActiveOrder
import ru.zveron.contract.order.external.CustomerActiveOrderKt
import ru.zveron.contract.order.external.CustomerCompletedOrder
import ru.zveron.contract.order.external.CustomerCompletedOrderKt
import ru.zveron.contract.order.external.GetCustomerResponse
import ru.zveron.contract.order.external.GetCustomerResponseKt
import ru.zveron.contract.order.external.GetOrderResponseKt
import ru.zveron.contract.order.external.GetOrdersByProfileResponseKt
import ru.zveron.contract.order.external.GetWaterfallResponseKt
import ru.zveron.contract.order.external.ProfileKt
import ru.zveron.contract.order.external.WaterfallOrderKt
import ru.zveron.contract.order.external.createOrderResponse
import ru.zveron.contract.order.external.customer
import ru.zveron.contract.order.external.customerActiveOrder
import ru.zveron.contract.order.external.customerCompletedOrder
import ru.zveron.contract.order.external.fullOrder
import ru.zveron.contract.order.external.getCustomerResponse
import ru.zveron.contract.order.external.getOrderResponse
import ru.zveron.contract.order.external.getOrdersByProfileResponse
import ru.zveron.contract.order.external.getWaterfallResponse
import ru.zveron.contract.order.model.AddressKt
import ru.zveron.contract.order.model.AnimalKt
import ru.zveron.entrpoint.mapper.CommonDtoMapper.of
import ru.zveron.persistence.model.constant.Status
import ru.zveron.service.model.FullOrderData
import ru.zveron.service.model.ProfileOrder
import ru.zveron.service.model.WaterfallOrderLot
import ru.zveron.util.ChronoFormatter
import ru.zveron.util.PriceFormatter

@Suppress("unused")
object ResponseMapper {

    fun GetOrdersByProfileResponseKt.of(data: List<ProfileOrder>) = getOrdersByProfileResponse {
        this.activeOrders.addAll(
            data.filter { Status.canAcceptOrder(it.status) }
                .map { CustomerActiveOrderKt.of(it) },
        )

        this.completedOrders.addAll(
            data.filter { !Status.canAcceptOrder(it.status) }
                .map { CustomerCompletedOrderKt.of(it) },
        )
    }

    fun CustomerCompletedOrderKt.of(data: ProfileOrder) = customerCompletedOrder {
        this.id = data.orderLotId
        this.animal = AnimalKt.of(data.animal)
        this.title = data.title
        this.price = data.price
        this.serviceDate = ChronoFormatter.formatServiceDate(data.serviceDateFrom, data.serviceDateTo)
        this.createdAt = ChronoFormatter.formatCreatedAt(data.createdAt)
    }

    fun CustomerActiveOrderKt.of(data: ProfileOrder) = customerActiveOrder {
        this.id = data.orderLotId
        this.animal = AnimalKt.of(data.animal)
        this.title = data.title
        this.price = data.price
        this.serviceDate = ChronoFormatter.formatServiceDate(data.serviceDateFrom, data.serviceDateTo)
        this.createdAt = ChronoFormatter.formatCreatedAt(data.createdAt)
    }

    fun GetOrderResponseKt.of(response: FullOrderData) = getOrderResponse {
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

    fun CreateOrderResponseKt.of(response: FullOrderData) = createOrderResponse {
        fullOrder = fullOrder {
            id = response.id
            profile = ProfileKt.of(response.profile)
            animal = AnimalKt.of(response.animal)
            response.subwayStation?.let { address = AddressKt.of(it) }
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
            waterfallOrderLots.map { WaterfallOrderKt.of(it) },
        )
    }

    fun GetCustomerResponseKt.of(serviceResponse: ru.zveron.service.model.GetCustomerResponse): GetCustomerResponse {
        return getCustomerResponse {
            this.customer = customer {
                this.id = serviceResponse.profile.id
                this.name = serviceResponse.profile.name
                this.rating = serviceResponse.profile.rating.toFloat()
                this.imageUrl = serviceResponse.profile.imageUrl
                this.activeOrders.addAll(
                    serviceResponse.orderLots.filter { Status.canAcceptOrder(it.status) }
                        .map { CustomerActiveOrderKt.of(it) },
                )
                this.completedOrders.addAll(
                    serviceResponse.orderLots.filter { it.status == Status.COMPLETED }
                        .map { CustomerCompletedOrderKt.of(it) },
                )
            }
        }
    }

    fun CustomerActiveOrderKt.of(serviceResponse: ru.zveron.service.model.CustomerProfileOrder): CustomerActiveOrder {
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

    fun CustomerCompletedOrderKt.of(serviceResponse: ru.zveron.service.model.CustomerProfileOrder): CustomerCompletedOrder {
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
