package ru.zveron.service.presentation.external

import com.google.protobuf.Empty
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import ru.zveron.FavoritesTest
import ru.zveron.client.profile.ProfileClient
import ru.zveron.commons.assertions.ProfileAssertions.profilesShouldBe
import ru.zveron.commons.generators.PrimitivesGenerator
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.generateProfileSummary
import ru.zveron.exception.FavoritesException
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class ProfileFavoritesComponentInternalExternalExternalTest : FavoritesTest() {

    @Autowired
    lateinit var profilesFavoritesService: ProfileFavoritesGrpcServiceExternal

    @Autowired
    lateinit var profilesFavoritesRepository: ProfilesFavoritesRecordRepository

    @TestConfiguration
    class InternalConfiguration {
        @Bean
        fun profileClient() = mockk<ProfileClient>()
    }

    @Autowired
    lateinit var profileClient: ProfileClient

    @Test
    fun `AddProfileToFavorites When adds someones profile to favorites Then it is added`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        runBlocking(MetadataElement(Metadata(profileId1))) {
            profilesFavoritesService.addToFavorites(
                ProfilesFavoritesRecordEntitiesGenerator.createAddProfileToFavoritesRequest(profileId2)
            )

            profilesFavoritesRepository.findById(
                ProfilesFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1,
                    profileId2
                )
            ).isPresent shouldBe true
        }
    }

    @Test
    fun `AddProfileToFavorites When unauthenticated`() {
        val (profileId2) = PrimitivesGenerator.generateNIds(1)
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.addToFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.createAddProfileToFavoritesRequest(profileId2)
                )
            }
        }
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `AddProfileToFavorites When adds myself to favorites Then got exception`() {
        val profileId1 = PrimitivesGenerator.generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                profilesFavoritesService.addToFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.createAddProfileToFavoritesRequest(profileId1)
                )
            }
        }

        exception.message shouldBe "Нельзя добавить себя в свой список избранного"
    }

    @Test
    fun `RemoveProfileFromFavorites When removes someones profile from favorites Then it is removed`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveProfile(profileId1, profileId2)
            profilesFavoritesService.removeFromFavorites(
                ProfilesFavoritesRecordEntitiesGenerator.createRemoveProfileFromFavoritesRequest(profileId2)
            )

            profilesFavoritesRepository.findById(
                ProfilesFavoritesRecordEntitiesGenerator.generateKey(
                    profileId1,
                    profileId2
                )
            ).isPresent shouldBe false
        }
    }

    @Test
    fun `RemoveProfileFromFavorites When unauthenticated`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                saveProfile(profileId1, profileId2)
                profilesFavoritesService.removeFromFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.createRemoveProfileFromFavoritesRequest(profileId2)
                )
            }
        }
        exception.message shouldBe "Authentication required"
    }

    @Test
    fun `RemoveProfileFromFavorites When removes not favorite profile from favorites Then got exception`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        val exception = shouldThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                profilesFavoritesService.removeFromFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.createRemoveProfileFromFavoritesRequest(profileId2)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить профиль не из списка избранного"
    }

    @Test
    fun `RemoveProfileFromFavorites When removes myself from favorites Then got exception`() {
        val profileId1 = PrimitivesGenerator.generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking(MetadataElement(Metadata(profileId1))) {
                profilesFavoritesService.removeFromFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.createRemoveProfileFromFavoritesRequest(profileId1)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить себя из своего списка избранного"
    }

    @Test
    fun `GetFavoriteProfiles When request for profiles Then appropriate profiles are returned`() {
        val (profileId1, profileId2, profileId3) = PrimitivesGenerator.generateNIds(3)
        val expectedProfiles = listOf(generateProfileSummary(profileId2))
        coEvery { profileClient.getProfilesSummary(listOf(profileId2)) } returns expectedProfiles
        runBlocking(MetadataElement(Metadata(profileId1))) {
            saveProfile(profileId1, profileId2)
            saveProfile(profileId3, profileId2)
            saveProfile(profileId2, profileId3)

            val profiles = profilesFavoritesService.getFavoriteProfiles(Empty.getDefaultInstance())
            profiles.favoriteProfilesList profilesShouldBe expectedProfiles
        }
    }

    @Test
    fun `GetFavoriteProfiles When unauthenticated`() {
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.getFavoriteProfiles(Empty.getDefaultInstance())
            }
        }
        exception.message shouldBe "Authentication required"
    }

    private fun saveProfile(ownerId: Long, targetId: Long) =
        profilesFavoritesRepository.save(
            ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(
                ownerId,
                targetId
            )
        )
}