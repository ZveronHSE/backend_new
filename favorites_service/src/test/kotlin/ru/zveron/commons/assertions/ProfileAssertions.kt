package ru.zveron.commons.assertions

import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
import ru.zveron.favorites.profile.ProfileSummary

object ProfileAssertions {

    infix fun List<ProfileSummary>.profilesShouldBe(expected: List<ru.zveron.contract.profile.ProfileSummary>) {
        this.size shouldBe expected.size
        this.forEach { actualLot ->
            val expectedLot = expected.firstOrNull { it.id == actualLot.id }
                ?: fail("Profile with id: ${actualLot.id} is missed in expected profiles")
            actualLot profileShouldBe expectedLot
        }
    }

    private infix fun ProfileSummary.profileShouldBe(expected: ru.zveron.contract.profile.ProfileSummary) {
        id shouldBe expected.id
        name shouldBe expected.name
        surname shouldBe expected.surname
        imageId shouldBe expected.imageId
    }
}