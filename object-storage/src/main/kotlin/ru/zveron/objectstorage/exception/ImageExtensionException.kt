package ru.zveron.objectstorage.exception

import io.grpc.Status

class ImageExtensionException(imageFormat: String) :
    ObjectStorageException(message = "Wrong image format $imageFormat", code = Status.Code.INVALID_ARGUMENT)
