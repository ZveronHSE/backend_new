package ru.zveron.domain.link

data class MailRuData(
    val email: String,
) : CommunicationLinkData {
    override val type: CommunicationLinkType = CommunicationLinkType.MAIL_RU
}
