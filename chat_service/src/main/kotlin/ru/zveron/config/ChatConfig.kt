package ru.zveron.config

import com.datastax.oss.driver.api.core.uuid.Uuids
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component("chatConfigBean")
@Suppress("unused")
class ChatConfig {

    final val nodeUuid = Uuids.timeBased()
    final val nodeUuidFormatted = nodeUuid.toString()

    init {
        MDC.put("node-id", nodeUuidFormatted)
    }
}