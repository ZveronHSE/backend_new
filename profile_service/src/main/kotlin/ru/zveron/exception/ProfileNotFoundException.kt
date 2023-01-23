package ru.zveron.exception

class ProfileNotFoundException(override val message: String) : ProfileException(message)