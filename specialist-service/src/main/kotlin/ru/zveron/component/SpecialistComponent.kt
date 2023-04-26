package ru.zveron.component

import org.springframework.stereotype.Component
import ru.zveron.entity.Specialist
import ru.zveron.expection.SpecialistNotFoundException
import ru.zveron.repository.SpecialistRepository

@Component
class SpecialistComponent(
    private val specialistRepository: SpecialistRepository
) {

    fun getSpecialistOrThrow(id: Long): Specialist {
        val specialist = specialistRepository.findById(id)
        if (specialist.isEmpty) {
            throw SpecialistNotFoundException(id)
        }

        return specialist.get()
    }
}