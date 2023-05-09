package ru.zveron.service

import org.hibernate.Hibernate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.contract.specialist.Sort
import ru.zveron.contract.specialist.manage.GetProfileResponse
import ru.zveron.entity.Specialist
import ru.zveron.expection.SpecialistNotFoundException
import ru.zveron.mapper.SpecialistMapper
import ru.zveron.model.SummarySpecialist
import ru.zveron.repository.SpecialistRepository
import ru.zveron.repository.waterfall.WaterfallRepository

@Service
class SpecialistService(
    private val specialistRepository: SpecialistRepository,
    private val waterfallRepository: WaterfallRepository
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

    fun getWaterfall(sort: Sort?, pageSize: Int): List<SummarySpecialist> {

        TODO("Not yet implemented")
    }

}