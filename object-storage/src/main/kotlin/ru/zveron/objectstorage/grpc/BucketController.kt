package ru.zveron.objectstorage.grpc

import net.devh.boot.grpc.server.service.GrpcService
import ru.zveron.contract.objectstorage.external.BucketServiceGrpcKt
import ru.zveron.contract.objectstorage.external.UploadImageRequest
import ru.zveron.contract.objectstorage.external.UploadImageResponse
import ru.zveron.contract.objectstorage.external.uploadImageResponse
import ru.zveron.objectstorage.grpc.mapper.BucketControllerMapper.toServiceRequest
import ru.zveron.objectstorage.service.BucketService

@GrpcService
class BucketController(
    private val bucketService: BucketService,
) : BucketServiceGrpcKt.BucketServiceCoroutineImplBase() {

    override suspend fun uploadImage(request: UploadImageRequest): UploadImageResponse {
        val imageAccessUrl = bucketService.uploadImageToBucket(request.toServiceRequest())

        return uploadImageResponse {
            this.imageUrl = imageAccessUrl
        }
    }
}
