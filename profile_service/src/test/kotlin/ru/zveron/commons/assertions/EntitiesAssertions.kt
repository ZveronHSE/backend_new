package ru.zveron.commons.assertions

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import ru.zveron.contract.address.AddressRequest
import ru.zveron.contract.address.AddressResponse
import ru.zveron.contract.profile.Address
import ru.zveron.contract.profile.GetProfileByChannelResponse
import ru.zveron.contract.profile.GetProfileInfoResponse
import ru.zveron.contract.profile.GetProfilePageResponse
import ru.zveron.contract.profile.GetProfileResponse
import ru.zveron.contract.profile.GetProfileWithContactsResponse
import ru.zveron.contract.profile.ProfileSummary
import ru.zveron.contract.profile.SetProfileInfoRequest
import ru.zveron.contract.profile.model.ChannelType
import ru.zveron.contract.profile.model.Links
import ru.zveron.domain.channel.ChannelsDto
import ru.zveron.domain.link.GmailData
import ru.zveron.domain.link.LinksDto
import ru.zveron.domain.link.VkData
import ru.zveron.entity.CommunicationLink
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings
import ru.zveron.mapper.ContactsMapper.toDto
import ru.zveron.mapper.ContactsMapper.toModel
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.abs

infix fun Profile.profileShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    addressId shouldBe expected.addressId
    passwordHash shouldBe expected.passwordHash
    ChronoUnit.MINUTES.between(expected.lastSeen, lastSeen) shouldBe 0
}

infix fun GetProfileResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    addressId shouldBe expected.addressId
    channelsList shouldBe expected.settings.channels.toModel()
}

infix fun GetProfileWithContactsResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    addressId shouldBe expected.addressId
    channelsList shouldBe expected.settings.channels.toModel()
    links linksShouldBe expected.communicationLinks.toDto()
    lastSeen timestampShouldBe expected.lastSeen
}

infix fun Links.linksShouldBe(expected: LinksDto) {
    vk.id shouldBe (expected.vkLink?.communicationLinkId ?: "")
    vk.ref shouldBe ((expected.vkLink?.data as? VkData)?.ref ?: "")
    vk.email shouldBe ((expected.vkLink?.data as? VkData)?.email ?: "")
    gmail.id shouldBe (expected.gmailLink?.communicationLinkId ?: "")
    gmail.email shouldBe ((expected.gmailLink?.data as? GmailData)?.email ?: "")
    phone.number shouldBe (expected.phoneLink?.communicationLinkId ?: "")
}

infix fun Settings.settingsShouldBe(expected: Settings) {
    id shouldBe expected.id
    searchAddressId shouldBe expected.searchAddressId
    channels shouldBe expected.channels
}

infix fun List<ChannelType>.channelsShouldBe(expected: ChannelsDto) {
    val set = this.toMutableSet()
    if (expected.vk) {
        set shouldContain ChannelType.VK
        set.remove(ChannelType.VK)
    }
    if (expected.gmail) {
        set shouldContain ChannelType.GOOGLE
        set.remove(ChannelType.GOOGLE)
    }
    if (expected.chat) {
        set shouldContain ChannelType.CHAT
        set.remove(ChannelType.CHAT)
    }
    if (expected.phone) {
        set shouldContain ChannelType.PHONE
        set.remove(ChannelType.PHONE)
    }
    set.size shouldBe 0
}

infix fun ChannelsDto.channelsShouldBe(expected: List<ChannelType>) {
    vk shouldBe expected.contains(ChannelType.VK)
    gmail shouldBe expected.contains(ChannelType.GOOGLE)
    phone shouldBe expected.contains(ChannelType.PHONE)
    chat shouldBe expected.contains(ChannelType.CHAT)
}

infix fun LinksDto.linksShouldBe(expected: LinksDto) {
    when (expected.vkLink) {
        null -> vkLink?.shouldBeNull()
        else -> vkLink linkShouldBe expected.vkLink!!
    }
    when (expected.gmailLink) {
        null -> gmailLink?.shouldBeNull()
        else -> gmailLink linkShouldBe expected.gmailLink!!
    }
    when (expected.phoneLink) {
        null -> phoneLink?.shouldBeNull()
        else -> phoneLink linkShouldBe expected.phoneLink!!
    }
}

infix fun CommunicationLink?.linkShouldBe(expected: CommunicationLink) {
    shouldNotBeNull()
    if (id != 0L && expected.id != 0L) {
        id shouldBe expected.id
    }
    communicationLinkId shouldBe expected.communicationLinkId
    data shouldBe expected.data
}

infix fun GetProfilePageResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    contacts.channelsList channelsShouldBe expected.settings.channels
    contacts.links linksShouldBe expected.communicationLinks.toDto()
    lastActivity timestampShouldBe expected.lastSeen
}

infix fun GetProfileInfoResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
}

infix fun GetProfilePageResponse.responseShouldBeBlockedAnd(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    contacts.channelsList.size shouldBe 0
    contacts.links shouldBe Links.getDefaultInstance()
    lastActivity timestampShouldBe expected.lastSeen
}

infix fun GetProfileByChannelResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
}

infix fun ProfileSummary.profileShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
    addressId shouldBe expected.addressId
}

infix fun Timestamp.timestampShouldBe(expected: Instant) {
    val expectedTimestamp = timestamp { seconds = expected.epochSecond; nanos = expected.nano }
    this.seconds shouldBe expectedTimestamp.seconds
    abs(this.nanos - expectedTimestamp.nanos) shouldBeLessThan 1000
}

infix fun Address.addressShouldBe(expected: AddressResponse) {
    region shouldBe expected.region
    town shouldBe expected.town
    latitude shouldBe expected.latitude
    longitude shouldBe expected.longitude
}

infix fun AddressRequest.addressShouldBe(expected: Address) {
    region shouldBe expected.region
    town shouldBe expected.town
    latitude shouldBe expected.latitude
    longitude shouldBe expected.longitude
}

infix fun Profile.profileShouldBe(expected: SetProfileInfoRequest) {
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageUrl shouldBe expected.imageUrl
}
