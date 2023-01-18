package ru.zveron.apigateway.registry

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors.FileDescriptor
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import io.grpc.protobuf.lite.ProtoLiteUtils
import io.grpc.reflection.v1alpha.ServerReflectionRequest
import io.grpc.reflection.v1alpha.ServerReflectionResponse
import kotlinx.coroutines.reactive.awaitSingle
import mu.KLogging
import org.springframework.cloud.netflix.eureka.reactive.EurekaReactiveDiscoveryClient
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class ProtoDefinitionRegistry(
    private val channelRegistry: GrpcChannelRegistry,
    private val eurekaClient: EurekaReactiveDiscoveryClient,
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

    private val serviceToFileDescriptorMap = ConcurrentHashMap<String, FileDescriptor>()

    private val serviceToProtoFile = ConcurrentHashMap<String, String?>()

    suspend fun getProtoFileDescriptor(serviceName: String, grpcServiceName: String) =
        serviceToFileDescriptorMap["$grpcServiceName-$serviceName"] ?: createProtoFileDescriptor(
            grpcServiceName,
            serviceName
        )

    private suspend fun createProtoFileDescriptor(
        protoServiceName: String,
        serviceName: String,
    ): FileDescriptor {
        val protoFileName = serviceToProtoFile["$protoServiceName-$serviceName"]
            ?: eurekaClient.getInstances(serviceName)
                .awaitSingle()?.metadata?.get(protoServiceName)
                .also {
                    serviceToProtoFile["$protoServiceName-$serviceName"] = it
                }
            ?: error("No service file provided in metadata")
        logger.debug { "Proto file path $protoFileName" }

        val serverReflectionRequest = ServerReflectionRequest.newBuilder()
            .setFileByFilename(protoFileName)
            .build()

        val channel = channelRegistry.getChannel(serviceName)

        val response = ClientCalls.unaryRpc(channel, reflectionMethodDescr, serverReflectionRequest)

        if (response.hasErrorResponse()) {
            logger.error { response.errorResponse }
            throw RuntimeException(response.errorResponse.errorMessage)
        }

        val fileDescriptorProtos = response.fileDescriptorResponse.fileDescriptorProtoList.map {
            DescriptorProtos.FileDescriptorProto.parseFrom(
                it
            )
        }

        val nameToProtoFile = fileDescriptorProtos.associateBy { it.name }

        fileDescriptorProtos
            .filter { it.name.equals(protoFileName, true) }
            .forEach { protoFile ->
                val dependencies = protoFile.dependencyList.takeUnless { it.isEmpty() }?.let { stringList ->
                    stringList.asByteStringList().map { nameToProtoFile[it.toStringUtf8()] }
                }?.map { protoDescr ->
                    FileDescriptor.buildFrom(protoDescr, arrayOf(), true)
                }?.toTypedArray() ?: emptyArray()

                val fileDescriptor = FileDescriptor.buildFrom(protoFile, dependencies, true)
                serviceToFileDescriptorMap["$protoServiceName-$serviceName"] = fileDescriptor
            }

        return serviceToFileDescriptorMap["$protoServiceName-$serviceName"]
            ?: error("Failed to initialize proto descriptor for $protoServiceName-$serviceName")
    }
}
