package ru.zveron.model

data class SummarySpecialist(
    val id: Long,
    val name: String,
    val surname: String,
    val patronymic: String,
    val description: String,
    val imgUrl: String
)