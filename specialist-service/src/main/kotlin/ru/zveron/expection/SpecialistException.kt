package ru.zveron.expection

open class SpecialistException(override val message: String, val code: io.grpc.Status) : RuntimeException(message)