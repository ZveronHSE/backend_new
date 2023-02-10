package ru.zveron.commons.generator

import ru.zveron.contract.profile.model.gmail
import ru.zveron.entity.Profile
import ru.zveron.contract.profile.model.links
import ru.zveron.contract.profile.model.phone
import ru.zveron.contract.profile.model.vk
import ru.zveron.domain.link.GmailData
import ru.zveron.domain.link.LinksDto
import ru.zveron.domain.link.PhoneData
import ru.zveron.domain.link.VkData
import ru.zveron.entity.CommunicationLink
import ru.zveron.mapper.ContactsMapper.toDto

object CommunicationLinksGenerator {

    fun generateLinks(
        profile: Profile,
        addVk: Boolean = false,
        addGmail: Boolean = false,
        addPhone: Boolean = false,
        skipVkRef: Boolean = false,
    ): LinksDto {
        val result = mutableListOf<CommunicationLink>()
        if (addVk) {
            result.add(
                CommunicationLink(
                    communicationLinkId = PropsGenerator.generateString(10),
                    data = VkData(
                        ref = if (skipVkRef) "" else PropsGenerator.generateString(15),
                        email = PropsGenerator.generateString(15),
                    ),
                    profile = profile,
                )
            )
        }
        if (addGmail) {
            result.add(
                CommunicationLink(
                    communicationLinkId = PropsGenerator.generateString(10),
                    data = GmailData(
                        email = PropsGenerator.generateString(15),
                    ),
                    profile = profile,
                )
            )
        }
        if (addPhone) {
            result.add(
                CommunicationLink(
                    communicationLinkId = PropsGenerator.generateString(10),
                    data = PhoneData(),
                    profile = profile,
                )
            )
        }

        profile.communicationLinks.addAll(result)
        return result.toDto()
    }

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