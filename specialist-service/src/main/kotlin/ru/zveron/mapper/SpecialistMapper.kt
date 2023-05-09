package ru.zveron.mapper

import ru.zveron.contract.specialist.manage.EditNameRequest
import ru.zveron.contract.specialist.manage.FullAchievement
import ru.zveron.contract.specialist.manage.FullEducation
import ru.zveron.contract.specialist.manage.FullOther
import ru.zveron.contract.specialist.manage.FullService
import ru.zveron.contract.specialist.manage.FullWorkExperience
import ru.zveron.contract.specialist.manage.getProfileResponse
import ru.zveron.entity.Achievement
import ru.zveron.entity.Education
import ru.zveron.entity.OtherInfo
import ru.zveron.entity.Service
import ru.zveron.entity.Specialist
import ru.zveron.entity.WorkExperience
import ru.zveron.model.NameSpecialist
import java.util.Locale

object SpecialistMapper {
    private const val DEFAULT_SIZE_OF_DESCRIPTION = 100

    fun of(specialist: Specialist) = getProfileResponse {
        id = specialist.id
        name = "${specialist.name} ${specialist.surname}"
        rating = "3.5 (3 оценки)"
        quantityReview = "14 отзывов"
        imgUrl = specialist.imgUrl
        description = specialist.description.take(DEFAULT_SIZE_OF_DESCRIPTION)

        educations.addAll(specialist.educations.map { ProfileInfoMapper.of(it) })
        workExperiences.addAll(specialist.workExperiences.map { ProfileInfoMapper.of(it) })
        achievements.addAll(specialist.achievements.map { ProfileInfoMapper.of(it) })
        others.addAll(specialist.otherInfo.map { ProfileInfoMapper.of(it) })
        services.addAll(specialist.services.map { ProfileInfoMapper.of(it) })

        documentUrls.addAll(specialist.documents.map { it.url })
    }

    fun FullAchievement.toEntity(specialist: Specialist, id: Long = -1): Achievement {
        return Achievement(
            id = id,
            title = title.uppercaseFirstLetter(),
            year = year,
            documentUrl = documentUrl,
            showPhoto = showPhoto,
            specialist = specialist
        )
    }

    fun FullEducation.toEntity(specialist: Specialist, id: Long = -1): Education {
        return Education(
            id = id,
            educationalInstitution = educationalInstitution.uppercaseFirstLetter(),
            faculty = faculty.uppercaseFirstLetter(),
            specialization = specialization.uppercaseFirstLetter(),
            startYear = startYear,
            endYear = endYear,
            diplomaUrl = diplomaUrl,
            showPhoto = showPhoto,
            specialist = specialist
        )
    }

    fun FullOther.toEntity(specialist: Specialist, id: Long = -1): OtherInfo {
        return OtherInfo(
            id = id,
            title = title.uppercaseFirstLetter(),
            documentUrl = documentUrl,
            showPhoto = showPhoto,
            specialist = specialist
        )
    }

    fun FullService.toEntity(specialist: Specialist, id: Long = -1): Service {
        return Service(
            id = id,
            title = title.uppercaseFirstLetter(),
            startPrice = startPrice,
            endPrice = endPrice,
            isRemotely = isRemotely,
            atHome = atHome,
            isHomeVisit = homeVisit,
            specialist = specialist
        )
    }

    fun FullWorkExperience.toEntity(specialist: Specialist, id: Long = -1): WorkExperience {
        return WorkExperience(
            id = id,
            organization = organization.uppercaseFirstLetter(),
            workTitle = workTitle.uppercaseFirstLetter(),
            startYear = startYear,
            endYear = endYear,
            documentUrl = documentUrl,
            specialist = specialist
        )
    }

    fun EditNameRequest.toNameSpecialist() = NameSpecialist(
        name = name.capitalizedWord(),
        surname = surname.capitalizedWord(),
        patronymic = patronymic.capitalizedWord()
    )

    private fun String.capitalizedWord(): String {
        return this.lowercase().uppercaseFirstLetter()
    }

    private fun String.uppercaseFirstLetter(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}