package ru.zveron.mapper

import ru.zveron.contract.specialist.manage.infoEntity
import ru.zveron.entity.Achievement
import ru.zveron.entity.Education
import ru.zveron.entity.OtherInfo
import ru.zveron.entity.Service
import ru.zveron.entity.WorkExperience

object InfoEntityMapper {
    private const val DEFAULT_SIZE_OF_TITLE = 50
    private const val MARK_RUBLE = "₽"
    private const val TITLE_IF_ANY_PRICE = "Договорная"
    private const val WORKING_UNTIL_NOW = "н. в."

    fun of(education: Education) = infoEntity {
        id = education.id
        title = "${education.specialization} (${education.startYear}-${education.endYear})"
    }

    fun of(workExperience: WorkExperience) = infoEntity {
        id = workExperience.id
        title = "${workExperience.workTitle} (${workExperience.startYear}-${buildEndDate(workExperience.endYear)})"
    }

    fun of(achievement: Achievement) = infoEntity {
        id = achievement.id
        title = achievement.title
    }

    fun of(otherInfo: OtherInfo) = infoEntity {
        id = otherInfo.id
        title = otherInfo.title.take(DEFAULT_SIZE_OF_TITLE)
    }

    fun of(service: Service) = infoEntity {
        id = service.id
        title = "${service.title} (${buildPrice(service.startPrice, service.endPrice)})"
    }

    private fun buildPrice(startPrice: Int?, endPrice: Int?): String {
        if (startPrice == null && endPrice == null) {
            return TITLE_IF_ANY_PRICE
        }

        if (endPrice == null) {
            return "$startPrice$MARK_RUBLE"
        }

        return "$startPrice$MARK_RUBLE - $endPrice$MARK_RUBLE"
    }

    private fun buildEndDate(year: Int?): String {
        if (year != null) {
            return "$year"
        }

        return WORKING_UNTIL_NOW
    }
}