package ru.zveron.commons.assertions

import com.google.protobuf.Timestamp
import com.google.protobuf.timestamp
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import ru.zveron.Address
import ru.zveron.ChannelType
import ru.zveron.GetProfileInfoResponse
import ru.zveron.GetProfilePageResponse
import ru.zveron.GetProfileResponse
import ru.zveron.GetProfileWithContactsResponse
import ru.zveron.Links
import ru.zveron.LotSummary
import ru.zveron.SetProfileInfoRequest
import ru.zveron.contract.AddressRequest
import ru.zveron.mapper.ContactsMapper
import ru.zveron.contract.AddressResponse
import ru.zveron.contract.lot.Lot
import ru.zveron.domain.ChannelsDTO
import ru.zveron.entity.Contact
import ru.zveron.entity.Profile
import ru.zveron.entity.Settings
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.math.abs

infix fun Profile.profileShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
    addressId shouldBe expected.addressId
    ChronoUnit.MINUTES.between(expected.lastSeen, lastSeen) shouldBe 0
}

infix fun GetProfileResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
    addressId shouldBe expected.addressId
    channelsList shouldBe ContactsMapper.channelsDTO2Model(expected.settings.channels)
}

infix fun GetProfileWithContactsResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
    addressId shouldBe expected.addressId
    channelsList shouldBe ContactsMapper.channelsDTO2Model(expected.settings.channels)
    links linksShouldBe expected.contact
}

infix fun Links.linksShouldBe(expected: Contact) {
    vk.ref shouldBe expected.vkRef
    vk.email shouldBe expected.additionalEmail
    gmail.email shouldBe expected.gmail
    phone.number shouldBe expected.phone
}

infix fun Settings.settingsShouldBe(expected: Settings) {
    id shouldBe expected.id
    searchAddressId shouldBe expected.searchAddressId
    channels shouldBe expected.channels
}

infix fun List<ChannelType>.channelsShouldBe(expected: ChannelsDTO) {
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

infix fun ChannelsDTO.channelsShouldBe(expected: List<ChannelType>) {
    vk shouldBe expected.contains(ChannelType.VK)
    gmail shouldBe expected.contains(ChannelType.GOOGLE)
    phone shouldBe expected.contains(ChannelType.PHONE)
    chat shouldBe expected.contains(ChannelType.CHAT)
}

infix fun Contact.contactShouldBe(expected: Contact) {
    id shouldBe expected.id
    vkRef shouldBe expected.vkRef
    gmail shouldBe expected.gmail
    additionalEmail shouldBe expected.additionalEmail
    phone shouldBe expected.phone
}

infix fun GetProfilePageResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
    contacts.channelsList channelsShouldBe expected.settings.channels
    contacts.links linksShouldBe expected.contact
    lastActivity timestampShouldBe expected.lastSeen
}

infix fun GetProfileInfoResponse.responseShouldBe(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
}

infix fun GetProfilePageResponse.responseShouldBeBlockedAnd(expected: Profile) {
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
    contacts.channelsList.size shouldBe 0
    contacts.links shouldBe Links.getDefaultInstance()
    lastActivity timestampShouldBe expected.lastSeen
}

infix fun LotSummary.lotShouldBe(expected: Lot) {
    id shouldBe expected.id
    title shouldBe expected.title
    priceFormatted shouldBe expected.price
    publicationDateFormatted shouldBe expected.publicationDate
    firstImage shouldBe expected.photoId
    isFavorite shouldBe expected.isFavorite
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
    id shouldBe expected.id
    name shouldBe expected.name
    surname shouldBe expected.surname
    imageId shouldBe expected.imageId
}