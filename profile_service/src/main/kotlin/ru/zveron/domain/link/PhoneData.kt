package ru.zveron.domain.link

data class PhoneData(
    override val type: CommunicationLinkType = CommunicationLinkType.PHONE
) : CommunicationLinkData