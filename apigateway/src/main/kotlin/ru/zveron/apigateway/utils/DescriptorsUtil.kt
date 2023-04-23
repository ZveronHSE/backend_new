package ru.zveron.apigateway.utils

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.MethodDescriptor
import io.grpc.protobuf.lite.ProtoLiteUtils

object DescriptorsUtil {

    fun Descriptors.MethodDescriptor.getGrpcMethodDescriptor(
        type: MethodDescriptor.MethodType = MethodDescriptor.MethodType.UNARY
    ): MethodDescriptor<DynamicMessage?, DynamicMessage> =
        MethodDescriptor.newBuilder<DynamicMessage, DynamicMessage>()
            .setFullMethodName(MethodDescriptor.generateFullMethodName(this.service.fullName, this.name))
            .setRequestMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(this.inputType)))
            .setResponseMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(this.outputType)))
            .setType(type)
            .build()

    fun Descriptors.FileDescriptor.getMethodDescriptor(
        protoService: String,
        protoMethod: String,
    ) =
        services.find { it.name.equals(protoService, true) }
            ?.methods
            ?.find { it.name.equals(protoMethod, true) }
            ?: error("Failed to find $protoMethod of $protoService for ${this.fullName}")

    fun Descriptors.MethodDescriptor.dynamicMessageBuilder(
        json: String,
    ): DynamicMessage.Builder? =
        DynamicMessage.newBuilder(this.inputType).apply {
            JsonFormat.parser().merge(json, this)
        }
}
