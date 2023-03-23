package ru.zveron.apigateway.component

import com.google.protobuf.DescriptorProtos
import com.google.protobuf.Descriptors.FileDescriptor
import io.grpc.MethodDescriptor
import io.grpc.kotlin.ClientCalls
import io.grpc.protobuf.lite.ProtoLiteUtils
import io.grpc.reflection.v1alpha.ServerReflectionRequest
import io.grpc.reflection.v1alpha.ServerReflectionResponse
import kotlinx.coroutines.reactive.awaitSingle
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.marker.Markers.append
import net.logstash.logback.marker.Markers.appendEntries
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
        grpcServiceName: String,
        serviceName: String,
    ): FileDescriptor {
        logger.debug(
            appendEntries(
                mapOf(
                    "serviceName" to serviceName,
                    "grpcServiceName" to grpcServiceName
                )
            )
        ) { "Creating new protoFile instance" }

        val protoFilePath = serviceToProtoFile["$grpcServiceName-$serviceName"]
            ?: eurekaClient.getInstances(serviceName)
                .awaitSingle()?.metadata?.get(grpcServiceName)
                .also {
                    serviceToProtoFile["$grpcServiceName-$serviceName"] = it
                }
            ?: error("No service file provided in metadata")

        logger.debug(append("path", protoFilePath)) { "Proto file path" }
        val serverReflectionRequest = ServerReflectionRequest.newBuilder()
            .setFileByFilename(protoFilePath)
            .build()

        logger.debug(append("serviceName", serviceName)) { "Requesting channel" }
        val channel = channelRegistry.getChannel(serviceName)

        logger.debug() { "Calling service reflection rpc to get description" }
        val response = ClientCalls.unaryRpc(channel, reflectionMethodDescr, serverReflectionRequest)

        if (response.hasErrorResponse()) {
            logger.error("Reflection rpc call failed with {}", keyValue("errorResponse", response.errorResponse))
            throw RuntimeException(response.errorResponse.errorMessage)
        }

        val fileDescriptorProtos = response.fileDescriptorResponse.fileDescriptorProtoList.map {
            DescriptorProtos.FileDescriptorProto.parseFrom(
                it
            )
        }

        val nameToProtoFile = fileDescriptorProtos.associateBy { it.name }

        logger.debug { "Mapping proto file to proto file descriptor" }
        //У каждого прото дескриптора есть зависимости, например тот же протобафный Empty, они нужны при составлении
        //дескриптора файла. В зависимостях у нас есть только имя, поэтому заранее маппим имя в описание файла
        //Все эти зависимости переводим в формат дескриптор файла и используем его как список зависимостей при создании нашего основного файла
        fileDescriptorProtos
            .find { it.name.equals(protoFilePath, true) }
            ?.let { protoFile ->
                val dependencies = protoFile.dependencyList.takeUnless { it.isEmpty() }
                logger.debug(append("dependencies", dependencies)) { "Proto file requires dependencies" }

                val dependenciesFileDescriptor = dependencies?.let { stringList ->
                    stringList.asByteStringList()
                        .map { protoFileName ->
                            nameToProtoFile[protoFileName.toStringUtf8()]
                        }
                }?.map { protoDescr ->
                    logger.debug(append("dependency", protoDescr?.name)) { "Parsing dependency" }
                    FileDescriptor.buildFrom(protoDescr, arrayOf(), true)
                }?.toTypedArray()
                    ?: emptyArray()

                logger.debug { "Building file descriptor for main proto file" }
                val fileDescriptor = FileDescriptor.buildFrom(protoFile, dependenciesFileDescriptor, true)

                serviceToFileDescriptorMap["$grpcServiceName-$serviceName"] = fileDescriptor
            }

        return serviceToFileDescriptorMap["$grpcServiceName-$serviceName"]
            ?: error("Failed to initialize proto descriptor for $grpcServiceName-$serviceName")
    }
}
