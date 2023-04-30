package ru.zveron.common.assertion

import com.google.protobuf.Timestamp
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.math.abs

object ProtoDataTypesAssertions {

    infix fun Timestamp.timestampShouldBe(expected: Timestamp) {
        seconds shouldBe expected.seconds
        abs(nanos - expected.nanos) shouldBeLessThan 1000000
    }
}