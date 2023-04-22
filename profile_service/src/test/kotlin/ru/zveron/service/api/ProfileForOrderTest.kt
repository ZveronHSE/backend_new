package ru.zveron.service.api

import com.ninjasquad.springmockk.MockkBean
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.ProfileTest
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.commons.generator.PropsGenerator
import ru.zveron.contract.profile.getProfileRequest
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.repository.ProfileRepository
import ru.zveron.service.client.review.ReviewClient

class ProfileForOrderTest @Autowired constructor(
    private val service: ProfileServiceInternal,
    private val profileRepository: ProfileRepository,
    private val template: TransactionTemplate,
) : ProfileTest() {

    @MockkBean
    lateinit var reviewClient: ReviewClient

    @Test
    fun `get profile for order smoke test`() {
        //prep data
        val expectedProfile = ProfileGenerator.generateProfile()
        val expectedRating = PropsGenerator.ratingGenerator()
        val profileId = template.execute {
            profileRepository.save(expectedProfile).id
        }

        val request = getProfileRequest {
            id = profileId!!
        }

        //prep env
        coEvery { reviewClient.getRating(any()) } returns expectedRating

        runBlocking(MetadataElement(Metadata(profileId))) {
            //when
            val response = service.getProfileForOrder(request)

            //then
            response.asClue {
                it.id shouldBe profileId
                it.name shouldBe expectedProfile.name
                it.imageUrl shouldBe expectedProfile.imageUrl
                it.rating shouldBe expectedRating.toFloat()
            }
        }
    }

}
