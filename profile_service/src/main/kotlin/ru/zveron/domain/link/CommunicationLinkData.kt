package ru.zveron.domain.link

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes(
        JsonSubTypes.Type(value = VkData::class, name = VK_COMMUNICATION_LINK_TYPE),
        JsonSubTypes.Type(value = GmailData::class, name = GMAIL_COMMUNICATION_LINK_TYPE),
        JsonSubTypes.Type(value = PhoneData::class, name = PHONE_COMMUNICATION_LINK_TYPE),
)
interface CommunicationLinkData {

    val type: CommunicationLinkType
}