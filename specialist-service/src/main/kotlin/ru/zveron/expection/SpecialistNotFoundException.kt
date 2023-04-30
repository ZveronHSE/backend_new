package ru.zveron.expection

import io.grpc.Status

class SpecialistNotFoundException(id: Long) : SpecialistException(
    message = "Didn't find specialist with ID: $id",
    code = Status.NOT_FOUND
)