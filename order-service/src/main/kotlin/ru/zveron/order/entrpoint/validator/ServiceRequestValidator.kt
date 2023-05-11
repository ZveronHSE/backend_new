package ru.zveron.order.entrpoint.validator

import ru.zveron.contract.order.external.GetWaterfallRequest

object ServiceRequestValidator {

    fun validate(request: GetWaterfallRequest) {
        takeIf { request.hasLastOrderId() }?.run { require(request.lastOrderId > 0) { "Last order id should be null or positive. Actual value ${request.lastOrderId}" } }
        require(request.pageSize in 1..100) { "Page size should be positive and less than 100. Actual value ${request.pageSize}" }
    }
}
