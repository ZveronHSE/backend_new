package ru.zveron.commons

import io.kotest.matchers.shouldBe
import ru.zveron.contract.blacklist.ProfileSummary

object Assertions {

    infix fun ProfileSummary.summaryShouldBe(expected: ru.zveron.contract.profile.ProfileSummary) {
        id shouldBe expected.id
        name shouldBe expected.name
        surname shouldBe expected.surname
        imageId shouldBe expected.imageId
        addressId shouldBe expected.addressId
    }
}