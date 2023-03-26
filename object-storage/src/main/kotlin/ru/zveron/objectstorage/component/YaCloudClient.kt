package ru.zveron.objectstorage.component

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import mu.KLogging
import net.logstash.logback.argument.StructuredArguments.kv
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import ru.zveron.objectstorage.component.model.UploadImageFailure
import ru.zveron.objectstorage.component.model.UploadImageResponse
import ru.zveron.objectstorage.component.model.UploadImageSuccess

@Component
class YaCloudClient(
    private val s3Client: S3Client,
) {

    companion object : KLogging()

    suspend fun uploadImage(
        imageKey: String,
        imageBucket: String,
        mediaTypeValue: String,
        imageBytes: ByteArray,
    ): UploadImageResponse {
        val request = PutObjectRequest {
            bucket = imageBucket
            key = imageKey
            body = imageBytes.let { ByteStream.fromBytes(it) }
            contentType = mediaTypeValue
            metadata = mutableMapOf<String, String>().apply {
                this[HttpHeaders.CONTENT_TYPE] = mediaTypeValue
            }
        }

        logger.debug(
            "Prepared a request and calling client for {} {}",
            kv("bucket", imageBucket),
            kv("key", imageKey)
        )

        return try {
            s3Client.use { s3 ->
                s3.putObject(request).also {
                    logger.debug("Request completed. {}", kv("response", it))
                }

            }

            UploadImageSuccess
        } catch (ex: Exception) {
            UploadImageFailure(ex.message ?: "no message", imageBucket, imageKey)
        }
    }
}
