package ru.zveron.exception

import java.lang.RuntimeException

open class ProfileException(override val message: String?): RuntimeException(message)