package ru.zveron.service.api

import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.ProfileTest
import ru.zveron.commons.generator.AnimalGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.contract.profile.getAnimalBatchRequest
import ru.zveron.contract.profile.getAnimalRequestInt
import ru.zveron.repository.AnimalRepository
import ru.zveron.repository.ProfileRepository

class AnimalServiceInternalTest @Autowired constructor(
    private val service: AnimalServiceInternal,
    private val template: TransactionTemplate,
    private val profileRepository: ProfileRepository,
    private val animalRepository: AnimalRepository,
) : ProfileTest() {


    @Test
    fun `given animalId when getAnimal succeeds, then return animal`() {
        //prep data
        val animal = AnimalGenerator.generateAnimal()
        val profile = ProfileGenerator.generateProfile()

        val animaId = template.execute {
            //prep env
            val savedProfile = profileRepository.save(profile)
            animalRepository.save(animal.copy(profile = savedProfile)).id
        }

        val request = getAnimalRequestInt { this.animalId = animaId!! }

        //when
        val response = template.execute {
            runBlocking {
                service.getAnimal(request)
            }
        }

        //then
        response.shouldNotBeNull().animal.asClue {
            it.shouldNotBeNull()
            it.age shouldBe animal.age
            it.breed shouldBe animal.breed
            it.name shouldBe animal.name
            it.imageUrlsList shouldBe animal.imageUrls.toList()
            it.documentUrlsList shouldBe animal.documentUrls.toList()
        }
    }

    @Test
    fun `given getAnimalBatchRequest when getAnimalBatch succeeds, then return animals`() {
        //prep data
        val animal1 = AnimalGenerator.generateAnimal()
        val animal2 = AnimalGenerator.generateAnimal()
        val profile = ProfileGenerator.generateProfile()

        val animalIds = template.execute {
            //prep env
            val savedProfile = profileRepository.save(profile)
            animalRepository.saveAll(listOf(animal1, animal2).map { it.copy(profile = savedProfile) }).map { it.id }
        }

        val request = getAnimalBatchRequest {
            this.animalIds.addAll(animalIds!!)
        }

        //when
        val response = template.execute {
            runBlocking {
                service.getAnimalBatch(request)
            }
        }

        //then
        response.shouldNotBeNull().animalsList.asClue {
            it.shouldNotBeNull()
            it.size shouldBe 2
            it[0].age shouldBe animal1.age
            it[0].breed shouldBe animal1.breed
            it[0].name shouldBe animal1.name
            it[0].imageUrlsList shouldBe animal1.imageUrls.toList()
            it[0].documentUrlsList shouldBe animal1.documentUrls.toList()
            it[1].age shouldBe animal2.age
            it[1].breed shouldBe animal2.breed
            it[1].name shouldBe animal2.name
            it[1].imageUrlsList shouldBe animal2.imageUrls.toList()
            it[1].documentUrlsList shouldBe animal2.documentUrls.toList()
        }
    }
}