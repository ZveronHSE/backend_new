package ru.zveron.commons.generator

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
        vkRef = if (addVk) PropsGenerator.generateString(15) else "",
        additionalEmail = if (addVk) PropsGenerator.generateString(15) else "",
        gmail = if (addGmail) PropsGenerator.generateString(15) else "",
        phone = if (addPhone) PropsGenerator.generateString(15) else "",
    ).also { profile.contact = it }
}