package ru.zveron.objectstorage.service

import mu.KLogging
import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.marker.Markers.append
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import ru.zveron.objectstorage.component.YaCloudClient
import ru.zveron.objectstorage.component.model.UploadImageFailure
import ru.zveron.objectstorage.exception.S3ClientException
import ru.zveron.objectstorage.grpc.constant.ZVERON_BUCKET_PREFIX
import ru.zveron.objectstorage.service.model.UploadImageRequest
import ru.zveron.objectstorage.util.UrlUtil
import java.util.UUID

@Service
class BucketService(
    private val yaCloudClient: YaCloudClient,
) {

    companion object : KLogging()

    suspend fun uploadImageToBucket(request: UploadImageRequest): String {
        val imageKey = UUID.randomUUID().toString()
        val imageBucket = """$ZVERON_BUCKET_PREFIX-${request.source.let { StringUtils.lowerCase(it.name) }}"""

        logger.debug(
            append("imageKey", imageKey)
                .and<LogstashMarker>(append("imageBucket", imageBucket))
        ) { "Making client request" }

        val uploadImageResponse = yaCloudClient.uploadImage(
            imageKey = imageKey,
            imageBucket = imageBucket,
            mediaTypeValue = request.imageExtension,
            imageBytes = request.imageBytes,
        )

        if (uploadImageResponse is UploadImageFailure) {
            throw S3ClientException(uploadImageResponse.bucket, uploadImageResponse.key, uploadImageResponse.message)
        }

        return UrlUtil.buildAccessUrl(bucket = imageBucket, key = imageKey)
    }
}
