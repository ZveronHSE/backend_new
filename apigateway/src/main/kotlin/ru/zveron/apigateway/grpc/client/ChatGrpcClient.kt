package ru.zveron.apigateway.grpc.client

import kotlinx.coroutines.flow.Flow
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import ru.zveron.contract.chat.ChatRouteRequest
import ru.zveron.contract.chat.ChatServiceExternalGrpcKt

@Component
class ChatGrpcClient {
    @GrpcClient("chat-service")
    lateinit var client: ChatServiceExternalGrpcKt.ChatServiceExternalCoroutineStub

    fun bidiChatRoute(requests: Flow<ChatRouteRequest>, headers: io.grpc.Metadata) =
        client.bidiChatRoute(requests, headers)
}