package ru.zveron.domain.link

data class VkData(
    val ref: String,
    val email: String,
) : CommunicationLinkData {
    override val type: CommunicationLinkType = CommunicationLinkType.VK
}
