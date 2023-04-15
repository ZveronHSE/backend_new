val testcontainersVersion: String by rootProject
val kotlinxVersion: String by rootProject
val springVersion: String by rootProject

configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jetty:$springVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")

    implementation("aws.sdk.kotlin:s3:0.20.2-beta")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.10")
    implementation("com.github.zveronHSe.contract:objectstorage:1.9.7")

    implementation("aws.smithy.kotlin:http-client-engine-okhttp-jvm:0.15.2")
}
