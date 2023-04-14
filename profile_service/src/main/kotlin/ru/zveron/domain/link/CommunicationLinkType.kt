package ru.zveron.domain.link

import com.fasterxml.jackson.annotation.JsonValue

const val VK_COMMUNICATION_LINK_TYPE = "vk-communication-link"
const val GMAIL_COMMUNICATION_LINK_TYPE = "gmail-communication-link"
const val PHONE_COMMUNICATION_LINK_TYPE = "phone-communication-link"
const val MAIL_RU_COMMUNICATION_LINK_TYPE = "mail-ru-communication-link"

enum class CommunicationLinkType(@field:JsonValue val typeName: String) {
    VK(VK_COMMUNICATION_LINK_TYPE),
    GMAIL(GMAIL_COMMUNICATION_LINK_TYPE),
    PHONE(PHONE_COMMUNICATION_LINK_TYPE),
    MAIL_RU(MAIL_RU_COMMUNICATION_LINK_TYPE),
    ;
}