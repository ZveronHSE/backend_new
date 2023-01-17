package ru.zveron.apigateway.grpc.registry

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors
import io.grpc.Channel
import io.grpc.ConnectivityState
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import io.grpc.protobuf.lite.ProtoLiteUtils
import io.grpc.reflection.v1alpha.ServerReflectionRequest
import io.grpc.reflection.v1alpha.ServerReflectionResponse
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ProtoDefinitionRegistry(
    private val channelRegistry: GrpcChannelRegistry,
) {

    companion object : KLogging()

    private val reflectionMethodDescr = MethodDescriptor.newBuilder<ServerReflectionRequest, ServerReflectionResponse>()
        .setFullMethodName(
            MethodDescriptor.generateFullMethodName(
                "grpc.reflection.v1alpha.ServerReflection",
                "ServerReflectionInfo"
            )
        )
        .setRequestMarshaller(ProtoLiteUtils.marshaller(ServerReflectionRequest.getDefaultInstance()))
        .setResponseMarshaller(ProtoLiteUtils.marshaller(ServerReflectionResponse.getDefaultInstance()))
        .setType(MethodDescriptor.MethodType.UNARY)
        .build()


    //todo: alias based map
    private val protoMap = ConcurrentHashMap<String, Descriptors.FileDescriptor>()

    private val serviceToProtoFile = ConcurrentHashMap<String, String>().apply {
        this["BlacklistService-blacklist-service"] = "crud.proto"
    }

    suspend fun getProtoFileDescriptor(protoServiceName: String, serviceName: String) =
        protoMap["$protoServiceName-$serviceName"] ?: createProtoFileDescriptor(protoServiceName, serviceName)

    private suspend fun createProtoFileDescriptor(
        protoServiceName: String,
        serviceName: String,
    ): Descriptors.FileDescriptor {
        val protoFile = serviceToProtoFile["$protoServiceName-$serviceName"]
        val serverReflectionRequest = ServerReflectionRequest.newBuilder()
            .setFileByFilename(protoFile)
            .build()


        val channel = channelRegistry.getChannel(serviceName)
        logger.info { channel.isShutdown }
        logger.info { channel.isTerminated }
        logger.info { channel.getState(true) }
        while (channel.getState(true).equals(ConnectivityState.TRANSIENT_FAILURE)){
            logger.error{ channel.getState(true) }
        }

        val response = ClientCalls.unaryRpc(channel, reflectionMethodDescr, serverReflectionRequest)
        if (response.hasErrorResponse()) {
            logger.error { response }
        }

        response.let { resp ->
            resp.fileDescriptorResponse.fileDescriptorProtoList.map {
                DescriptorProtos.FileDescriptorProto.parseFrom(
                    it
                )
            }
                .forEach { file ->
                    file?.serviceList?.forEach { protoService ->
                        protoMap["${protoService.name}-$serviceName"] =
                            Descriptors.FileDescriptor.buildFrom(file, protoMap.values.toTypedArray(), true)
                    }
                }
        }

        return protoMap["$protoServiceName-$serviceName"] ?: error("Failed to create new proto file descriptions")
    }

}
