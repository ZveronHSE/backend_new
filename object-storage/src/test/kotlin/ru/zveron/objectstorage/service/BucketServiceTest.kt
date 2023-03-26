package ru.zveron.objectstorage.service

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldStartWith
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import ru.zveron.objectstorage.component.YaCloudClient
import ru.zveron.objectstorage.component.model.UploadImageFailure
import ru.zveron.objectstorage.component.model.UploadImageSuccess
import ru.zveron.objectstorage.exception.S3ClientException
import ru.zveron.objectstorage.service.model.UploadImageRequest
import ru.zveron.objectstorage.util.YANDEX_CLOUD_HOST
import ru.zveron.objectstorage.util.randomBucket
import ru.zveron.objectstorage.util.randomEnum
import ru.zveron.objectstorage.util.randomImageBytes
import ru.zveron.objectstorage.util.randomKey
import ru.zveron.objectstorage.util.testUploadImageRequest

class BucketServiceTest {
    private val yaCloudClient = mockk<YaCloudClient>()

    private val service = BucketService(
        yaCloudClient = yaCloudClient,
    )

    @Test
    fun `given correct upload request, when client and repository succeed, then return image metadata id`() {
        //prep data
        val request = UploadImageRequest(
            imageBytes = randomImageBytes(),
            imageExtension = MediaType.IMAGE_JPEG_VALUE,
            source = randomEnum(),
        )

        //prep env
        coEvery { yaCloudClient.uploadImage(any(), any(), any(), any()) } returns UploadImageSuccess

        //when
        val response = runBlocking {
            //then
            shouldNotThrowAny {
                service.uploadImageToBucket(request)
            }
        }

        //then
        response shouldStartWith YANDEX_CLOUD_HOST
    }

    @Test
    fun `given correct upload request, when client fails to upload the image, then throw exception`() {
        //prep data
        val request = testUploadImageRequest()
        val key = randomKey()
        val bucket = randomBucket()

        val clientFailureResponse = UploadImageFailure("fail", bucket, key)

        //prep env
        coEvery { yaCloudClient.uploadImage(any(), any(), any(), any()) } returns clientFailureResponse

        //when
        runBlocking {
            //then
            shouldThrow<S3ClientException> {
                service.uploadImageToBucket(request)
            }
        }
    }
}
