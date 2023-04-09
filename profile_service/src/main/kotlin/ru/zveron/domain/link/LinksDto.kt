package ru.zveron.domain.link

import ru.zveron.entity.CommunicationLink

data class LinksDto(
    val phoneLink: CommunicationLink? = null,
    val vkLink: CommunicationLink? = null,
    val gmailLink: CommunicationLink? = null,
    val mailRuLink: CommunicationLink? = null,
)
