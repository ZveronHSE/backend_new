package ru.zveron.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.zveron.component.SpecialistComponent
import ru.zveron.contract.specialist.manage.Documents
import ru.zveron.contract.specialist.manage.EditAchievementRequest
import ru.zveron.contract.specialist.manage.EditEducationRequest
import ru.zveron.contract.specialist.manage.EditNameRequest
import ru.zveron.contract.specialist.manage.EditOtherRequest
import ru.zveron.contract.specialist.manage.EditServiceRequest
import ru.zveron.contract.specialist.manage.EditWorkExperienceRequest
import ru.zveron.contract.specialist.manage.FullAchievement
import ru.zveron.contract.specialist.manage.FullEducation
import ru.zveron.contract.specialist.manage.FullOther
import ru.zveron.contract.specialist.manage.FullService
import ru.zveron.contract.specialist.manage.FullWorkExperience
import ru.zveron.contract.specialist.manage.InfoEntity
import ru.zveron.contract.specialist.manage.documents
import ru.zveron.entity.Document
import ru.zveron.expection.SpecialistIllegalArgumentException
import ru.zveron.expection.SpecialistOutOfRangeException
import ru.zveron.mapper.InfoEntityMapper
import ru.zveron.mapper.SpecialistMapper.toEntity
import ru.zveron.mapper.SpecialistMapper.toNameSpecialist
import ru.zveron.model.NameSpecialist
import ru.zveron.repository.AchievementRepository
import ru.zveron.repository.DocumentRepository
import ru.zveron.repository.EducationRepository
import ru.zveron.repository.OtherInfoRepository
import ru.zveron.repository.ServiceRepository
import ru.zveron.repository.SpecialistRepository
import ru.zveron.repository.WorkExperienceRepository

const val MINIMUM_YEAR = 1950
const val MAX_SIZE_OF_TITLE = 200
const val MAX_SIZE_OF_NAME = 30

@Service
class ManageSpecialistService(
    private val achievementRepository: AchievementRepository,
    private val documentRepository: DocumentRepository,
    private val educationRepository: EducationRepository,
    private val otherInfoRepository: OtherInfoRepository,
    private val serviceRepository: ServiceRepository,
    private val specialistRepository: SpecialistRepository,
    private val workExperienceRepository: WorkExperienceRepository,
    private val specialistComponent: SpecialistComponent
) {
    @Transactional
    fun editDescription(specialistID: Long, description: String): String {
        specialistComponent.getSpecialistOrThrow(specialistID)

        if (description.length > 500) {
            throw SpecialistOutOfRangeException("description")
        }

        specialistRepository.setDescription(specialistID, description)

        return description
    }

    fun addAchievement(specialistID: Long, request: FullAchievement): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        request.validate()

        val achievement = achievementRepository.save(request.toEntity(specialist))

        return InfoEntityMapper.of(achievement)
    }

    @Transactional
    fun editAchievement(specialistID: Long, request: EditAchievementRequest): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        request.achievement.validate()

        val achievement = achievementRepository.save(request.achievement.toEntity(specialist, request.id))

        return InfoEntityMapper.of(achievement)
    }

    @Transactional
    fun deleteAchievement(specialistID: Long, id: Long) {
        specialistComponent.getSpecialistOrThrow(specialistID)

        achievementRepository.deleteById(id)
    }

    fun addEducation(specialistID: Long, fullEducation: FullEducation): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        fullEducation.validate()

        val education = educationRepository.save(fullEducation.toEntity(specialist))

        return InfoEntityMapper.of(education)
    }

    @Transactional
    fun editEducation(specialistID: Long, educationRequest: EditEducationRequest): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        educationRequest.education.validate()

        val education = educationRequest.education
            .toEntity(specialist, educationRequest.id)
            .also {
                educationRepository.save(it)
            }

        return InfoEntityMapper.of(education)
    }

    fun deleteEducation(specialistID: Long, id: Long) {
        specialistComponent.getSpecialistOrThrow(specialistID)

        educationRepository.deleteById(id)
    }

    fun addOther(specialistID: Long, fullOther: FullOther): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        fullOther.validate()

        val otherInfo = fullOther.toEntity(specialist).also {
            otherInfoRepository.save(it)
        }

        return InfoEntityMapper.of(otherInfo)
    }

    @Transactional
    fun editOther(specialistID: Long, request: EditOtherRequest): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)


        val otherInfo = request.other
            .toEntity(specialist, request.id)
            .also {
                otherInfoRepository.save(it)
            }

        return InfoEntityMapper.of(otherInfo)
    }

    @Transactional
    fun deleteOther(specialistID: Long, id: Long) {
        specialistComponent.getSpecialistOrThrow(specialistID)

        educationRepository.deleteById(id)
    }

    fun addService(specialistID: Long, serviceRequest: FullService): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        serviceRequest.validate()

        val service = serviceRequest
            .toEntity(specialist)
            .also {
                serviceRepository.save(it)
            }

        return InfoEntityMapper.of(service)
    }

    @Transactional
    fun editService(specialistID: Long, serviceRequest: EditServiceRequest): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        serviceRequest.service.validate()

        val service = serviceRequest.service
            .toEntity(specialist, serviceRequest.id)
            .also {
                serviceRepository.save(it)
            }

        return InfoEntityMapper.of(service)
    }

    @Transactional
    fun deleteService(specialistID: Long, id: Long) {
        specialistComponent.getSpecialistOrThrow(specialistID)

        serviceRepository.deleteById(id)
    }


    fun addWorkExperience(specialistID: Long, workRequest: FullWorkExperience): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        workRequest.validate()

        val workExperience = workRequest
            .toEntity(specialist)
            .also {
                workExperienceRepository.save(it)
            }

        return InfoEntityMapper.of(workExperience)
    }

    @Transactional
    fun editWorkExperience(specialistID: Long, workRequest: EditWorkExperienceRequest): InfoEntity {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        workRequest.work.validate()

        val workExperience = workRequest.work
            .toEntity(specialist, workRequest.id)
            .also {
                workExperienceRepository.save(it)
            }

        return InfoEntityMapper.of(workExperience)
    }

    @Transactional
    fun deleteWorkExperience(specialistID: Long, id: Long) {
        specialistComponent.getSpecialistOrThrow(specialistID)

        workExperienceRepository.deleteById(id)
    }

    @Transactional
    fun editDocuments(specialistID: Long, documents: Documents): Documents {
        val specialist = specialistComponent.getSpecialistOrThrow(specialistID)

        val documentEntities = mutableListOf<Document>()
        documents.urlsList.map {
            it.validateUrl()
            documentEntities.add(Document(url = it, specialist = specialist))
        }

        documentRepository.deleteAllBySpecialist(specialist)
        val newEntities = documentRepository.saveAll(documentEntities)

        return documents {
            urls.addAll(newEntities.map { it.url })
        }
    }

    @Transactional
    fun editName(specialistID: Long, request: EditNameRequest): NameSpecialist {
        specialistComponent.getSpecialistOrThrow(specialistID)

        request.validate()

        return request
            .toNameSpecialist()
            .also {
                specialistRepository.editName(
                    specialistID,
                    it.name,
                    it.surname,
                    it.patronymic
                )
            }
    }

    private fun EditNameRequest.validate() {
        if (name.length > MAX_SIZE_OF_NAME) {
            throw SpecialistOutOfRangeException("name")
        }

        if (surname.length > MAX_SIZE_OF_NAME) {
            throw SpecialistOutOfRangeException("surname")
        }

        if (patronymic.length > MAX_SIZE_OF_NAME) {
            throw SpecialistOutOfRangeException("patronymic")
        }
    }

    private fun FullAchievement.validate() {
        if (title.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("title")
        }

        if (year <= MINIMUM_YEAR) {
            throw SpecialistIllegalArgumentException("year", year)
        }

        documentUrl.validateUrl()
    }

    private fun FullService.validate() {
        if (title.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("title")
        }

        if (hasEndPrice() && hasStartPrice()) {
            if (startPrice >= endPrice) {
                throw SpecialistIllegalArgumentException("startPrice", startPrice)
            }
        }
    }

    private fun FullOther.validate() {
        if (title.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("title")
        }

        documentUrl.validateUrl()
    }

    private fun FullEducation.validate() {
        if (educationalInstitution.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("educationalInstitution")
        }

        if (faculty.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("faculty")
        }

        if (specialization.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("specialization")
        }

        if (startYear <= MINIMUM_YEAR) {
            throw SpecialistIllegalArgumentException("startYear", startYear)
        }

        if (hasEndYear()) {
            if (endYear <= MINIMUM_YEAR) {
                throw SpecialistIllegalArgumentException("endYear", endYear)
            }

            if (startYear >= endYear) {
                throw SpecialistIllegalArgumentException("startYear", startYear)
            }
        }

        diplomaUrl.validateUrl()
    }

    private fun FullWorkExperience.validate() {
        if (organization.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("organization")
        }

        if (workTitle.length > MAX_SIZE_OF_TITLE) {
            throw SpecialistOutOfRangeException("workTitle")
        }

        if (startYear <= MINIMUM_YEAR) {
            throw SpecialistIllegalArgumentException("startYear", startYear)
        }

        if (hasEndYear()) {
            if (endYear <= MINIMUM_YEAR) {
                throw SpecialistIllegalArgumentException("endYear", endYear)
            }

            if (startYear >= endYear) {
                throw SpecialistIllegalArgumentException("startYear", startYear)
            }
        }


        documentUrl.validateUrl()
    }

    // TODO - перенести проверку валидации на запросы к клиенту по имейджу
    private fun String.validateUrl() {

    }
}