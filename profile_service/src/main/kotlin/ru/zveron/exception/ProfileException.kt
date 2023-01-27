package ru.zveron.exception

import io.grpc.Status
import java.lang.RuntimeException

open class ProfileException(override val message: String, val code: Status.Code): RuntimeException("Exception with message=$message and code=$code")