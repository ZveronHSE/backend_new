package ru.zveron.objectstorage.component

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectResponse
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import ru.zveron.objectstorage.component.model.UploadImageFailure
import ru.zveron.objectstorage.component.model.UploadImageSuccess
import ru.zveron.objectstorage.util.randomBucket
import ru.zveron.objectstorage.util.randomImageBytes
import ru.zveron.objectstorage.util.randomImageMediaType
import ru.zveron.objectstorage.util.randomKey

class YaCloudClientTest {
    private val s3Client = mockk<S3Client>()

    private val client = YaCloudClient(s3Client = s3Client)


    @Test
    fun `given correct upload request, when s3client uploads successfully, then return upload success`() {
        //prep data
        val imageKey = randomKey()
        val imageBucket = randomBucket()
        val bytes = randomImageBytes()
        val mediaType = randomImageMediaType()

        //prep env
        coEvery { s3Client.use { s3 -> s3.putObject(any()) } } returns PutObjectResponse {}

        //when
        val response = runBlocking {
            shouldNotThrowAny {
                client.uploadImage(
                    imageKey = imageKey,
                    imageBucket = imageBucket,
                    mediaTypeValue = mediaType,
                    imageBytes = bytes
                )
            }
        }

        //then
        response shouldBe UploadImageSuccess
    }

    @Test
    fun `given correct upload request, when s3client fails, then return upload failure`() {
        //prep data
        val imageKey = randomKey()
        val imageBucket = randomBucket()
        val bytes = randomImageBytes()
        val mediaType = randomImageMediaType()
        val message = "upload failed because no reason"

        //prep env
        coEvery { s3Client.use { s3 -> s3.putObject(any()) } } throws Exception(message)

        //when
        val response = runBlocking {
            shouldNotThrowAny {
                client.uploadImage(
                    imageKey = imageKey,
                    imageBucket = imageBucket,
                    mediaTypeValue = mediaType,
                    imageBytes = bytes
                )
            }
        }

        //then
        val expectedResponse = UploadImageFailure(message = message, bucket = imageBucket, key = imageKey)
        response shouldBe expectedResponse
    }
}
