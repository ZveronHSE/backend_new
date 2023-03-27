package ru.zveron.objectstorage.service.model

import ru.zveron.objectstorage.service.constant.FlowSource

data class UploadImageRequest(
    val imageBytes: ByteArray,
    val imageExtension: String,
    val source: FlowSource,
)