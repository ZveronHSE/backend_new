package ru.zveron.objectstorage.grpc

import com.google.protobuf.kotlin.toByteString
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.mockk.coEvery
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.zveron.contract.objectstorage.external.FlowSource
import ru.zveron.contract.objectstorage.external.uploadImageRequest
import ru.zveron.objectstorage.component.model.UploadImageSuccess
import ru.zveron.objectstorage.config.BaseObjectStorageTest
import ru.zveron.objectstorage.util.randomImageBytes


class ObjectUploadSmokeTest : BaseObjectStorageTest() {

    @Autowired
    lateinit var bucketController: BucketController

    @Test
    fun `given correct upload image request, when client and database succeed, then return image metadata id`() {
        //prep data
        val grpcRequest = uploadImageRequest {
            this.body = randomImageBytes().toByteString()
            this.flowSource = FlowSource.values().asIterable().filter { it.number != -1 }.shuffled().first()
        }

        //prep env
        coEvery { yaCloudClient.uploadImage(any(), any(), any(), any()) } returns UploadImageSuccess

        //when
        runBlocking {
            //then
            shouldNotThrowAny {
                bucketController.uploadImage(request = grpcRequest)
            }
        }
    }
}
