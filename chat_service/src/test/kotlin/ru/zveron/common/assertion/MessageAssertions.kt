package ru.zveron.common.assertion

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.equality.shouldBeEqualToIgnoringFields
import io.kotest.matchers.shouldBe
import ru.zveron.common.assertion.ProtoDataTypesAssertions.timestampShouldBe
import ru.zveron.contract.chat.GetChatMessagesResponse
import ru.zveron.model.entity.Message
import java.time.temporal.ChronoUnit

object MessageAssertions {

    infix fun Message.messageShouldBe(expected: Message) {
        this.shouldBeEqualToIgnoringFields(expected, Message::receivedAt)
        ChronoUnit.SECONDS.between(this.receivedAt, expected.receivedAt) shouldBe 0
    }

    infix fun ru.zveron.contract.chat.model.Message.messageShouldBe(expected: ru.zveron.contract.chat.model.Message) {
        id shouldBe expected.id
        text shouldBe expected.text
        isRead shouldBe expected.isRead
        senderId shouldBe expected.senderId
        imagesUrlsList.shouldContainExactly(expected.imagesUrlsList)
        sentAt timestampShouldBe expected.sentAt
    }

    infix fun GetChatMessagesResponse.responseShouldBe(expected: GetChatMessagesResponse) {
        chatId shouldBe expected.chatId
        this.messagesList.size shouldBe expected.messagesList.size

        this.messagesList.forEachIndexed { index, message ->
            message messageShouldBe expected.messagesList[index]
        }
    }
}