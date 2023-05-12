package ru.zveron.order.entrpoint.validator

import com.google.type.Date
import com.google.type.TimeOfDay
import ru.zveron.contract.order.external.CreateOrderRequest
import ru.zveron.contract.order.external.GetWaterfallRequest

object ServiceRequestValidator {

    fun validate(request: GetWaterfallRequest) {
        takeIf { request.hasLastOrderId() }?.run { require(request.lastOrderId > 0) { "Last order id should be null or positive. Actual value ${request.lastOrderId}" } }
        require(request.pageSize in 1..100) { "Page size should be positive and less than 100. Actual value ${request.pageSize}" }
    }

    fun validate(request: CreateOrderRequest) {
        require(request.profileId > 0) { "Profile id should be positive. Actual value ${request.profileId}" }
        require(request.animalId > 0) { "Animal id should be positive. Actual value ${request.animalId}" }
        require(request.price >= 0) { "Price should be positive. Actual value ${request.price}" }
        require(request.title.isNotBlank()) { "Title should not be blank. Actual value ${request.title}" }
        require(request.description.isNotBlank()) { "Description should not be blank. Actual value ${request.description}" }
        takeIf { request.hasSubwayStationId() }?.run {
            require(request.subwayStationId > 0) {
                "Subway station id should be positive. Actual value ${request.subwayStationId}"
            }
        }

        validateServiceTime(request.serviceTimeFrom)
        validateServiceTime(request.serviceTimeTo)

        validateServiceDate(request.serviceDateFrom)
        validateServiceDate(request.serviceDateTo)
    }

    private fun validateServiceTime(serviceTime: TimeOfDay) {
        require(serviceTime.hours in 0..23) { "Service time should be less than 24. Actual value ${serviceTime.hours}" }
        require(serviceTime.minutes in 0..59) { "Service time should be less than 60. Actual value ${serviceTime.minutes}" }
        require(serviceTime.seconds in 0..59) { "Service time should be less than 60. Actual value ${serviceTime.seconds}" }
    }

    private fun validateServiceDate(serviceDate: Date) {
        require(serviceDate.year > 0) { "Service date should be positive. Actual value ${serviceDate.year}" }
        require(serviceDate.month in 1..12) { "Service date should be less than 13. Actual value ${serviceDate.month}" }
        require(serviceDate.day in 1..31) { "Service date should be less than 32. Actual value ${serviceDate.day}" }
    }
}
