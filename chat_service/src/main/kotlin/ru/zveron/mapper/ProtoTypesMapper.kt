package ru.zveron.mapper

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import ru.zveron.exception.InvalidParamChatException
import ru.zveron.model.dao.ChatRequestContext
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*

object ProtoTypesMapper {

    fun Timestamp.toInstant(): Instant =
        Instant.ofEpochSecond(seconds, nanos.toLong())

    fun Instant.toTimestamp(): Timestamp = timestamp {
        seconds = epochSecond
        nanos = nano
    }

    fun String.toUUID(context: ChatRequestContext): UUID =
        try{
            UUID.fromString(this)
        } catch (e: IllegalArgumentException) {
            throw InvalidParamChatException("Invalid argument: $this is not valid UUID", context)
        }
}