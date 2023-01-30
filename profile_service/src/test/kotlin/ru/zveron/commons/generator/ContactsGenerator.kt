package ru.zveron.commons.generator

import ru.zveron.contract.profile.model.gmail
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.entity.Contact
import ru.zveron.entity.Profile

object ContactsGenerator {

    fun generateContact(
        profile: Profile,
        addVk: Boolean = false,
        addGmail: Boolean = false,
        addPhone: Boolean = false,
    ) = Contact(
        id = profile.id,
        profile = profile,
        vkId = if (addVk) PropsGenerator.generateString(10) else "",
        vkRef = if (addVk) PropsGenerator.generateString(15) else "",
        additionalEmail = if (addVk) PropsGenerator.generateString(15) else "",
        gmailId = if (addVk) PropsGenerator.generateString(10) else "",
        gmail = if (addGmail) PropsGenerator.generateString(15) else "",
        phone = if (addPhone) PropsGenerator.generateString(10) else "",
    ).also { profile.contact = it }

    fun generateLinks(
        phone: String = "",
        vkId: String = "",
        vkRef: String = "",
        additionalEmail: String = "",
        gmailId: String = "",
        gmail: String = "",
    ) = links {
        this.phone = phone {
            number = phone
        }
        vk = vk {
            id = vkId
            ref = vkRef
            email = additionalEmail
        }
        this.gmail = gmail {
            id = gmailId
            email = gmail
        }
    }
}
