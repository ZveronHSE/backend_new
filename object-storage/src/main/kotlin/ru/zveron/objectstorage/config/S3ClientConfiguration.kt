package ru.zveron.objectstorage.config

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.http.engine.okhttp.OkHttpEngine
import aws.smithy.kotlin.runtime.net.Url
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated
import kotlin.time.Duration.Companion.seconds

@Configuration
@EnableConfigurationProperties(S3ClientProperties::class)
class S3ClientConfiguration(
    private val properties: S3ClientProperties,
) {

    @Bean
    fun s3Client(): S3Client {
        val credsProvider = StaticCredentialsProvider {
            this.secretAccessKey = properties.secretAccessKey
            this.accessKeyId = properties.accessKeyId
        }

        val httpEngine = OkHttpEngine {
            this.maxConnections = 64u
            this.connectTimeout = 10.seconds
        }

        val client = S3Client {
            this.region = properties.region
            this.endpointUrl = properties.endpointUrl.let { Url.parse(it) }
            this.credentialsProvider = credsProvider
            this.httpClientEngine = httpEngine
        }

        return client
    }
}

@Validated
@ConstructorBinding
@ConfigurationProperties("s3client")
data class S3ClientProperties(
    val accessKeyId: String,
    val secretAccessKey: String,
    val region: String,
    val endpointUrl: String,
)
