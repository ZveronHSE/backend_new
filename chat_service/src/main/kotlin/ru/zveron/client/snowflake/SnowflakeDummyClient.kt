package ru.zveron.client.snowflake

import com.datastax.oss.driver.api.core.uuid.Uuids
import org.springframework.stereotype.Component

@Component
class SnowflakeDummyClient: SnowflakeClient {

    override suspend fun fetchUuid() = Uuids.timeBased()
}