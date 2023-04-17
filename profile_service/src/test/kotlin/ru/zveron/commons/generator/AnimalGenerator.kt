package ru.zveron.commons.generator

import org.apache.commons.lang3.RandomUtils
import ru.zveron.entity.Animal

object AnimalGenerator {
    fun generateAnimal(
        animalId: Long = PropsGenerator.generateLongId(),
        name: String = PropsGenerator.generateString(10),
        breed: String = PropsGenerator.generateString(10),
        age: Int = RandomUtils.nextInt(),
        ownerId: Long = PropsGenerator.generateLongId(),
    ) = Animal(
        id = animalId,
        name = name,
        breed = breed,
        age = age,
        species = PropsGenerator.generateString(10),
        imageUrls = arrayOf(PropsGenerator.generateString(10)),
        documentUrls = arrayOf(PropsGenerator.generateString(10)),
        profile = ProfileGenerator.generateProfile().copy(id = ownerId),
    )
}