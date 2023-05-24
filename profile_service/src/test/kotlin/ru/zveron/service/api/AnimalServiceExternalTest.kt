package ru.zveron.service.api

import com.google.protobuf.Empty
import io.kotest.assertions.asClue
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import ru.zveron.ProfileTest
import ru.zveron.commons.generator.AnimalGenerator
import ru.zveron.commons.generator.ProfileGenerator
import ru.zveron.contract.profile.getAnimalRequestExt
import ru.zveron.library.grpc.interceptor.model.MetadataElement
import ru.zveron.library.grpc.model.Metadata
import ru.zveron.mapper.AnimalMapper.toEntityDoc
import ru.zveron.repository.AnimalRepository
import ru.zveron.repository.ProfileRepository

class AnimalServiceExternalTest @Autowired constructor(
    private val service: AnimalServiceExternal,
    private val profileRepository: ProfileRepository,
    private val animalRepository: AnimalRepository,
    private val transactionTemplate: TransactionTemplate,
) : ProfileTest() {

    companion object : KLogging()

    @Test
    fun `given animalId when getAnimal succeeds, then return animal`() {
        //prep data
        val animal = AnimalGenerator.generateAnimal()
        val profile = ProfileGenerator.generateProfile()

        val animaId = transactionTemplate.execute {
            //prep env
            val savedProfile = profileRepository.save(profile)
            animalRepository.save(animal.copy(profile = savedProfile)).id
        }

        val request = getAnimalRequestExt { this.animalId = animaId!! }

        //when
        val response = transactionTemplate.execute {
            runBlocking {
                logger.debug { animalRepository.findAll() }
                service.getAnimal(request)
            }
        }

        //then
        response.shouldNotBeNull().animal.asClue {
            it.shouldNotBeNull()
//            it.age shouldBe animal.age
            it.breed shouldBe animal.breed
            it.name shouldBe animal.name
            it.imageUrlsList shouldBe animal.imageUrls.toList()
            it.documentsList.map { it.toEntityDoc() } shouldBe animal.documentUrls.toList()
        }
    }

    @Test
    fun `given get animals by profile request, when metadata contains profileId and animals are present, then returns animals`() {
        //prep data
        val animals = List(3) { AnimalGenerator.generateAnimal() }

        //prep env
        val profile = transactionTemplate.execute {
            profileRepository.save(ProfileGenerator.generateProfile())
        }

        transactionTemplate.executeWithoutResult {
            animals.forEach { animalRepository.save(it.copy(profile = profile!!)) }
        }

        //when
        logger.debug { profile }
        val response = transactionTemplate.execute {
            runBlocking(MetadataElement(Metadata(profile!!.id))) {
                service.getAnimalsByProfile(Empty.getDefaultInstance())
            }
        }

        //then
        response.shouldNotBeNull().asClue {
            it.animalsList.size shouldBe animals.size
        }
    }

}