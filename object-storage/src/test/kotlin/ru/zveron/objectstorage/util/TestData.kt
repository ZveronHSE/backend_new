package ru.zveron.objectstorage.util

import org.apache.commons.lang3.RandomUtils
import ru.zveron.objectstorage.service.model.UploadImageRequest


fun testUploadImageRequest() = UploadImageRequest(
    imageBytes = RandomUtils.nextBytes(10),
    imageExtension = randomImageMediaType(),
    source = randomEnum(),
)
