package ru.zveron.domain.link

data class PhoneData(
    val passwordHash : String = "",
) : CommunicationLinkData {
    override val type: CommunicationLinkType = CommunicationLinkType.PHONE
}