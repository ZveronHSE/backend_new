package ru.zveron.expection

import io.grpc.Status

class SpecialistIllegalArgumentException(field: String, id: Any) : SpecialistException(
    message = "Illegal argument's for $field with value $id",
    code = Status.INVALID_ARGUMENT
)