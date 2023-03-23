package ru.zveron.objectstorage.util

const val YANDEX_CLOUD_HOST = """https://storage.yandexcloud.net"""

object UrlUtil {

    fun buildAccessUrl(bucket: String, key: String) = """$YANDEX_CLOUD_HOST/$bucket/$key"""
}
