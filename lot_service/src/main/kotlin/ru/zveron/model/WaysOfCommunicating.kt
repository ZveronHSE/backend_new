package ru.zveron.model

import java.io.Serializable

data class WaysOfCommunicating(
    var phone: Boolean = false,
    var facebook: Boolean = false,
    var vk: Boolean = false,
    var email: Boolean = false,
    var chat: Boolean = false
) : Serializable {
    /**
     * Проверяет, чтобы выбранных способов для связи было либо 1, либо 2.
     */
    fun validateNoMoreTwoWays(): Boolean {
        val ways = arrayOf(phone, facebook, vk, email, chat)

        return ways.count { it } in 1..2
    }
}