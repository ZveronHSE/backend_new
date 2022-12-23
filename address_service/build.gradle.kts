plugins {
}

val springVersion: String by rootProject

dependencies {
    implementation("com.github.zveronHSe:contract:feature~ZV-218_address-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
