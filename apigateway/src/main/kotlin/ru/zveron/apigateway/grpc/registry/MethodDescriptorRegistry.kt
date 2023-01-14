package ru.zveron.apigateway.grpc.registry

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import io.grpc.MethodDescriptor
import io.grpc.protobuf.lite.ProtoLiteUtils
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class MethodDescriptorRegistry {
    companion object : KLogging()

    private var descriptors = ConcurrentHashMap<String, MethodDescriptor<DynamicMessage, DynamicMessage>>()

    fun resolveDescriptor(
        protoServicePath: String,
        protoMethodName: String,
        methodDescriptor: Descriptors.MethodDescriptor,
    ): MethodDescriptor<DynamicMessage, DynamicMessage> =
        descriptors["$protoServicePath-$protoMethodName"] ?: createNewDescriptor(protoServicePath, protoMethodName, methodDescriptor).also {
            descriptors["$protoServicePath-$protoMethodName"] = it
        }


    //todo: should be generic type builder
    private fun createNewDescriptor(
        protoServicePath: String,
        protoMethodName: String,
        methodDescriptor: Descriptors.MethodDescriptor,
    ) =
        MethodDescriptor.newBuilder<DynamicMessage, DynamicMessage>()
            .setFullMethodName(MethodDescriptor.generateFullMethodName(protoServicePath, protoMethodName))
            .setRequestMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(methodDescriptor.inputType)))
            .setResponseMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(methodDescriptor.outputType)))
            .setType(MethodDescriptor.MethodType.UNARY)
            .build()

}