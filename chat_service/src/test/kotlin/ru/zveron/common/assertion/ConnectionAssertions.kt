package ru.zveron.common.assertion

import io.kotest.matchers.shouldBe
import ru.zveron.model.entity.Connection
import java.time.temporal.ChronoUnit

object ConnectionAssertions {

    infix fun Connection.connectionShouldBe(expected: Connection) {
        profileId shouldBe expected.profileId
        nodeAddress shouldBe expected.nodeAddress
        isClosed shouldBe expected.isClosed
        ChronoUnit.SECONDS.between(lastStatusChange, expected.lastStatusChange) shouldBe 0
    }
}