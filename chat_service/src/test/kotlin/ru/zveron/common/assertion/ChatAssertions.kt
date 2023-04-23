package ru.zveron.common.assertion

import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import ru.zveron.common.assertion.MessageAssertions.messageShouldBe
import ru.zveron.common.assertion.ProtoDataTypesAssertions.timestampShouldBe
import ru.zveron.contract.chat.GetRecentChatsResponse
import ru.zveron.contract.chat.ReceiveChatSummary
import ru.zveron.contract.chat.model.ChatFolder
import ru.zveron.contract.chat.model.ProfileSummary
import ru.zveron.contract.core.Lot
import ru.zveron.model.entity.Chat
import java.time.temporal.ChronoUnit

object ChatAssertions {

    infix fun Chat.chatShouldBe(expected: Chat) {
        this.shouldBeEqualToIgnoringFields(expected, Chat::lastUpdate)
        ChronoUnit.SECONDS.between(this.lastUpdate, expected.lastUpdate) shouldBe 0
    }

    infix fun ru.zveron.contract.chat.model.Chat.chatShouldBe(expected: ru.zveron.contract.chat.model.Chat) {
        chatId shouldBe expected.chatId
        unreadMessages shouldBe expected.unreadMessages
        lastUpdate timestampShouldBe expected.lastUpdate
        serviceId shouldBe expected.serviceId
        reviewId shouldBe expected.reviewId

        interlocutorSummary profileShouldBe expected.interlocutorSummary
        lotsList.forEachIndexed { index, lot ->
            lot lotShouldBe expected.lotsList[index]
        }
        messagesList.forEachIndexed { index, message ->
            message messageShouldBe expected.messagesList[index]
        }
        folder shouldBe expected.folder

        isBlocked shouldBe expected.isBlocked
    }

    infix fun Lot.lotShouldBe(expected: Lot) {
        id shouldBe expected.id
        title shouldBe expected.title
        price shouldBe expected.price
        publicationDate shouldBe expected.publicationDate
        imageUrl shouldBe expected.imageUrl
        favorite shouldBe expected.favorite
    }

    infix fun GetRecentChatsResponse.responseShouldBe(expected: GetRecentChatsResponse) {
        this.chatsList.size shouldBe expected.chatsList.size

        this.chatsList.forEachIndexed { index, chat ->
            chat chatShouldBe expected.chatsList[index]
        }
    }

    infix fun ReceiveChatSummary.responseShouldBe(expected: ReceiveChatSummary) {
        this.chat chatShouldBe expected.chat
    }

    infix fun ProfileSummary.profileShouldBe(expected: ProfileSummary) {
        id shouldBe expected.id
        name shouldBe expected.name
        surname shouldBe expected.surname
        imageUrl shouldBe expected.imageUrl
        isOnline shouldBe expected.isOnline
        formattedOnlineStatus shouldBe expected.formattedOnlineStatus
        lastOnline timestampShouldBe expected.lastOnline
    }

    fun ru.zveron.contract.chat.model.Chat.newChatShouldBe(
        profileSummary: ProfileSummary,
        lotSummary: Lot,
        message: String,
        userId: Long,
    ) {
        unreadMessages shouldBe 0
        serviceId shouldBe 0L
        reviewId shouldBe 0L
        folder shouldBe ChatFolder.NONE
        isBlocked shouldBe false

        interlocutorSummary profileShouldBe profileSummary

        lotsList.size shouldBe 1
        lotsList.first() lotShouldBe lotSummary

        messagesList.size shouldBe 1
        messagesList.first().apply {
            text shouldBe message
            isRead shouldBe false
            imagesUrlsList.isEmpty() shouldBe true
            senderId shouldBe userId
        }
    }
}