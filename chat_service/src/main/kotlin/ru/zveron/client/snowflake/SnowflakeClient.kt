package ru.zveron.client.snowflake

import java.util.*

interface SnowflakeClient {

    suspend fun fetchUuid(): UUID
}