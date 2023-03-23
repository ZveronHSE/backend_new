package ru.zveron.objectstorage.grpc.mapper

import org.springframework.http.MediaType
import ru.zveron.contract.objectstorage.external.FlowSource
import ru.zveron.contract.objectstorage.external.MimeType
import ru.zveron.contract.objectstorage.external.UploadImageRequest
import ru.zveron.objectstorage.exception.FlowSourceException
import ru.zveron.objectstorage.exception.ImageExtensionException

object BucketControllerMapper {
    fun UploadImageRequest.toServiceRequest() = ru.zveron.objectstorage.service.model.UploadImageRequest(
        imageBytes = this.body.toByteArray(),
        imageExtension = this.mimeType.toServiceType(),
        source = this.flowSource.toServiceSource(),
    )

    private fun MimeType.toServiceType() = when (this) {
        MimeType.IMAGE_JPEG -> MediaType.IMAGE_JPEG_VALUE
        MimeType.IMAGE_PNG -> MediaType.IMAGE_PNG_VALUE
        MimeType.UNRECOGNIZED -> throw ImageExtensionException(this.name)
    }

    private fun FlowSource.toServiceSource() = when (this) {
        FlowSource.ORDER -> ru.zveron.objectstorage.service.constant.FlowSource.ORDER
        FlowSource.PROFILE -> ru.zveron.objectstorage.service.constant.FlowSource.PROFILE
        FlowSource.LOT -> ru.zveron.objectstorage.service.constant.FlowSource.LOT
        FlowSource.CHAT -> ru.zveron.objectstorage.service.constant.FlowSource.CHAT
        FlowSource.UNRECOGNIZED -> throw FlowSourceException(this.name)
    }
}
