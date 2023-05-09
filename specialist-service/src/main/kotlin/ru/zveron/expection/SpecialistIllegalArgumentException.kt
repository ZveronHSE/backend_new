package ru.zveron.expection

import io.grpc.Status

class SpecialistIllegalArgumentException(field: String, value: Any) : SpecialistException(
    message = "Illegal argument's for $field with value $value",
    code = Status.INVALID_ARGUMENT
)