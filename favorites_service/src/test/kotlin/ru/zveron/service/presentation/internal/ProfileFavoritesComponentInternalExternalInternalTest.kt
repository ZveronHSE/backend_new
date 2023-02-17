package ru.zveron.service.presentation.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.FavoritesTest
import ru.zveron.commons.generators.PrimitivesGenerator
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class ProfileFavoritesComponentInternalExternalInternalTest : FavoritesTest() {

    @Autowired
    lateinit var profilesFavoritesService: ProfileFavoritesGrpcServiceInternal

    @Autowired
    lateinit var profilesFavoritesRepository: ProfilesFavoritesRecordRepository

    @Test
    fun `ProfileExistsInFavorites When checks if someones profile exists in favorites And it is exists Then returns true`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        runBlocking {
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId1, profileId2))
            val result = profilesFavoritesService.existsInFavorites(
                ProfilesFavoritesRecordEntitiesGenerator.crateProfileExistsInFavoritesRequest(profileId1, profileId2)
            )

            result.isExists shouldBe true
        }
    }

    @Test
    fun `ProfileExistsInFavorites When checks if someones profile exists in favorites And it doesn't exist Then returns false`() {
        val (profileId1, profileId2) = PrimitivesGenerator.generateNIds(2)
        runBlocking {
            val result = profilesFavoritesService.existsInFavorites(
                ProfilesFavoritesRecordEntitiesGenerator.crateProfileExistsInFavoritesRequest(profileId1, profileId2)
            )

            result.isExists shouldBe false
        }
    }

    @Test
    fun `ProfileExistsInFavorites When checks if my profile exists in favorites Then got exception`() {
        val profileId1 = PrimitivesGenerator.generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.existsInFavorites(
                    ProfilesFavoritesRecordEntitiesGenerator.crateProfileExistsInFavoritesRequest(profileId1, profileId1)
                )
            }
        }

        exception.message shouldBe "Профиль не может быть в собственном списке избранного"
    }

    @Test
    fun `RemoveAllProfilesByOwner When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2, profileId3) = PrimitivesGenerator.generateNIds(3)
        runBlocking {
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId1, profileId2))
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId1, profileId3))
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId2, profileId1))

            profilesFavoritesService.removeAllByOwner(ProfilesFavoritesRecordEntitiesGenerator.createRemoveAllProfilesByOwnerRequest(profileId1))

            val ids = profilesFavoritesRepository.findAll().map { it.id.ownerUserId }.toSet()
            ids.shouldContainExactly(profileId2)
        }
    }

    @Test
    fun `RemoveAllByFavoriteProfile When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2, profileId3) = PrimitivesGenerator.generateNIds(3)
        runBlocking {
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId1, profileId2))
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId1, profileId3))
            profilesFavoritesRepository.save(ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords(profileId2, profileId3))

            profilesFavoritesService.removeAllByFavoriteProfile(ProfilesFavoritesRecordEntitiesGenerator.createRemoveAllByFavoriteProfileRequest(profileId3))

            val ids = profilesFavoritesRepository.findAll().map { it.id.favoriteUserId }.toSet()
            ids.shouldContainExactly(profileId2)
        }
    }
}