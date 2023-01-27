package ru.zveron.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.FavoritesTest
import ru.zveron.commons.generators.IdsGenerator.generateNIds
import ru.zveron.commons.generators.IdsGenerator.generateUserId
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.crateProfileExistsInFavoritesRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.createAddProfileToFavoritesRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.createListFavoritesProfilesRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.createRemoveAllByFavoriteProfileRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.createRemoveAllProfilesByOwnerRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.createRemoveProfileFromFavoritesRequest
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.generateKey
import ru.zveron.commons.generators.ProfilesFavoritesRecordEntitiesGenerator.generateProfileRecords
import ru.zveron.exception.FavoritesException
import ru.zveron.repository.ProfilesFavoritesRecordRepository

@Suppress("BlockingMethodInNonBlockingContext")
class ProfilesFavoritesServiceTest : FavoritesTest() {

    @Autowired
    lateinit var profilesFavoritesService: ProfilesFavoritesService

    @Autowired
    lateinit var profilesFavoritesRepository: ProfilesFavoritesRecordRepository

    @Test
    fun `AddProfileToFavorites When adds someones profile to favorites Then it is added`() {
        val (profileId1, profileId2) = generateNIds(2)
        runBlocking {
            profilesFavoritesService.addToFavorites(
                createAddProfileToFavoritesRequest(profileId1, profileId2)
            )

            profilesFavoritesRepository.findById(generateKey(profileId1, profileId2)).isPresent shouldBe true
        }
    }

    @Test
    fun `AddProfileToFavorites When adds myself to favorites Then got exception`() {
        val profileId1 = generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.addToFavorites(
                    createAddProfileToFavoritesRequest(profileId1, profileId1)
                )
            }
        }

        exception.message shouldBe "Нельзя добавить себя в свой список избранного"
    }

    @Test
    fun `RemoveProfileFromFavorites When removes someones profile from favorites Then it is removed`() {
        val (profileId1, profileId2) = generateNIds(2)
        runBlocking {
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId2))
            profilesFavoritesService.removeFromFavorites(
                createRemoveProfileFromFavoritesRequest(profileId1, profileId2)
            )

            profilesFavoritesRepository.findById(generateKey(profileId1, profileId2)).isPresent shouldBe false
        }
    }

    @Test
    fun `RemoveProfileFromFavorites When removes not favorite profile from favorites Then got exception`() {
        val (profileId1, profileId2) = generateNIds(2)
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.removeFromFavorites(
                    createRemoveProfileFromFavoritesRequest(profileId1, profileId2)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить профиль не из списка избранного"
    }

    @Test
    fun `RemoveProfileFromFavorites When removes myself from favorites Then got exception`() {
        val profileId1 = generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.removeFromFavorites(
                    createRemoveProfileFromFavoritesRequest(profileId1, profileId1)
                )
            }
        }

        exception.message shouldBe "Нельзя удалить себя из своего списка избранного"
    }

    @Test
    fun `ProfileExistsInFavorites When checks if someones profile exists in favorites And it is exists Then returns true`() {
        val (profileId1, profileId2) = generateNIds(2)
        runBlocking {
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId2))
            val result = profilesFavoritesService.existsInFavorites(
                crateProfileExistsInFavoritesRequest(profileId1, profileId2)
            )

            result.isExists shouldBe true
        }
    }

    @Test
    fun `ProfileExistsInFavorites When checks if someones profile exists in favorites And it doesn't exist Then returns false`() {
        val (profileId1, profileId2) = generateNIds(2)
        runBlocking {
            val result = profilesFavoritesService.existsInFavorites(
                crateProfileExistsInFavoritesRequest(profileId1, profileId2)
            )

            result.isExists shouldBe false
        }
    }

    @Test
    fun `ProfileExistsInFavorites When checks if my profile exists in favorites Then got exception`() {
        val profileId1 = generateUserId()
        val exception = shouldThrow<FavoritesException> {
            runBlocking {
                profilesFavoritesService.existsInFavorites(
                    crateProfileExistsInFavoritesRequest(profileId1, profileId1)
                )
            }
        }

        exception.message shouldBe "Профиль не может быть в собственном списке избранного"
    }

    @Test
    fun `ListFavoriteProfiles When requests for favorite profiles Then appropriate profiles are returned`() {
        val (profileId1, profileId2, profileId3) = generateNIds(3)
        runBlocking {
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId2))
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId3))
            profilesFavoritesRepository.save(generateProfileRecords(profileId2, profileId1))

            val list = profilesFavoritesService.getFavoriteProfiles(createListFavoritesProfilesRequest(profileId1))

            list.favoriteProfilesList.map { it.id }.shouldContainExactlyInAnyOrder(profileId2, profileId3)
        }
    }

    @Test
    fun `RemoveAllProfilesByOwner When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2, profileId3) = generateNIds(3)
        runBlocking {
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId2))
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId3))
            profilesFavoritesRepository.save(generateProfileRecords(profileId2, profileId1))

            profilesFavoritesService.removeAllByOwner(createRemoveAllProfilesByOwnerRequest(profileId1))

            val ids = profilesFavoritesRepository.findAll().map { it.id.ownerUserId }.toSet()
            ids.shouldContainExactly(profileId2)
        }
    }

    @Test
    fun `RemoveAllByFavoriteProfile When removes Than appropriate records should be removed`() {
        val (profileId1, profileId2, profileId3) = generateNIds(3)
        runBlocking {
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId2))
            profilesFavoritesRepository.save(generateProfileRecords(profileId1, profileId3))
            profilesFavoritesRepository.save(generateProfileRecords(profileId2, profileId3))

            profilesFavoritesService.removeAllByFavoriteProfile(createRemoveAllByFavoriteProfileRequest(profileId3))

            val ids = profilesFavoritesRepository.findAll().map { it.id.favoriteUserId }.toSet()
            ids.shouldContainExactly(profileId2)
        }
    }
}
