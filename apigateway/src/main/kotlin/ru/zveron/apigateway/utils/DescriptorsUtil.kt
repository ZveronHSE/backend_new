package ru.zveron.apigateway.utils

import com.google.protobuf.Descriptors
import com.google.protobuf.DynamicMessage
import com.google.protobuf.util.JsonFormat
import io.grpc.MethodDescriptor
import io.grpc.protobuf.lite.ProtoLiteUtils

object DescriptorsUtil {

    private const val PROFILE_ID = "profile_id"

    fun Descriptors.MethodDescriptor.getGrpcMethodDescriptor() =
        MethodDescriptor.newBuilder<DynamicMessage, DynamicMessage>()
            .setFullMethodName(MethodDescriptor.generateFullMethodName(this.service.fullName, this.name))
            .setRequestMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(this.inputType)))
            .setResponseMarshaller(ProtoLiteUtils.marshaller(DynamicMessage.getDefaultInstance(this.outputType)))
            .setType(MethodDescriptor.MethodType.UNARY)
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
        id: Long?,
    ): DynamicMessage.Builder? {
        val builder = DynamicMessage.newBuilder(this.inputType).apply {
            JsonFormat.parser().merge(json, this)
        }

        this.inputType.fields.find { it.name== PROFILE_ID }?.let {descriptor ->
            id?.let { builder.setField(descriptor, it) }
        }

        return builder
    }
}
