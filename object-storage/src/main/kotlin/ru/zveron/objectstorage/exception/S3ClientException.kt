package ru.zveron.objectstorage.exception

import io.grpc.Status

class S3ClientException(bucket: String, key: String, clientMessage: String) : ObjectStorageException(
    message = "Client failed to save image for bucket=$bucket and key=$key with $clientMessage",
    code = Status.Code.INTERNAL,
)
