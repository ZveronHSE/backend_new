package ru.zveron.domain.link

data class GmailData(
    val email: String,
) : CommunicationLinkData {
    override val type: CommunicationLinkType = CommunicationLinkType.GMAIL
}
