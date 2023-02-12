package ru.zveron.util

import io.grpc.Status
import ru.zveron.exception.LotException

object ValidateUtils {
    fun Long.validatePositive(nameField: String) {
        if (this <= 0) {
            throw LotException(
                Status.INVALID_ARGUMENT,
                "Field $nameField should has only positive value"
            )
        }
    }

    fun Int.validatePositive(nameField: String) {
        if (this <= 0) {
            throw LotException(
                Status.INVALID_ARGUMENT,
                "Field $nameField should has only positive value"
            )
        }
    }
}