package ru.zveron.expection

import io.grpc.Status

class SpecialistOutOfRangeException(field: String) : SpecialistException(
    message = "Length of field $field overhead",
    code = Status.OUT_OF_RANGE
)