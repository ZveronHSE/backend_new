package ru.zveron.service

import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.contract.specialist.manage.GetProfileResponse
import ru.zveron.entity.Specialist
import ru.zveron.expection.SpecialistNotFoundException
import ru.zveron.mapper.SpecialistMapper
import ru.zveron.repository.SpecialistRepository

@Service
class SpecialistService(
    private val specialistRepository: SpecialistRepository
) {
    @Transactional
    fun getProfileSpecialist(id: Long): GetProfileResponse {
        val specialist = specialistRepository.findById(id)
        if (specialist.isEmpty) {
            throw SpecialistNotFoundException(id)
        }

        return SpecialistMapper.of(specialist = specialist.get().also { it.initAllField() })
    }


    private fun Specialist.initAllField() {
        Hibernate.initialize(educations)
        Hibernate.initialize(workExperiences)
        Hibernate.initialize(achievements)
        Hibernate.initialize(services)
        Hibernate.initialize(documents)
        Hibernate.initialize(otherInfo)
    }

}