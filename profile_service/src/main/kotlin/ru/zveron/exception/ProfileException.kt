package ru.zveron.exception

import java.lang.RuntimeException

class ProfileException(override val message: String?): RuntimeException(message)