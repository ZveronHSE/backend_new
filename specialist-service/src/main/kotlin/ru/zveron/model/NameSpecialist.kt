package ru.zveron.model

data class NameSpecialist(
    val name: String,
    val surname: String,
    val patronymic: String
) {
    override fun toString(): String {
        return "$name $surname"
    }
}