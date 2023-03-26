package ru.zveron.objectstorage.component.model

sealed class UploadImageResponse
data class UploadImageFailure(
    val message: String,
    val bucket: String,
    val key: String,
) : UploadImageResponse()

object UploadImageSuccess : UploadImageResponse()