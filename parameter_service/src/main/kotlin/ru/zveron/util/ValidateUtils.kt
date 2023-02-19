package ru.zveron.util

import io.grpc.Status
import ru.zveron.exception.ParameterException

object ValidateUtils {
    fun Long.validatePositive(nameField: String) {
        if (this <= 0) {
            throw ParameterException(
                "Field $nameField should has only positive value",
                Status.INVALID_ARGUMENT
            )
        }
    }

    fun Int.validatePositive(nameField: String) {
        if (this <= 0) {
            throw ParameterException(
                "Field $nameField should has only positive value",
                Status.INVALID_ARGUMENT
            )
        }
    }
}