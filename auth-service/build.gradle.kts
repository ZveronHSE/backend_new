val testcontainersVersion: String by rootProject
val kotlinxVersion: String by rootProject
val springVersion: String by rootProject

configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
    implementation.get().exclude("org.springframework", "spring-mvc:5.3.23")
}

dependencies {

    //reactive server
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty:$springVersion")

    //webflux to call third party clients
    implementation("org.springframework:spring-webflux:5.3.19")

    //coroutines with reactor
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")

    //reactive database
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    //utils
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.4")
    implementation("com.nimbusds:nimbus-jose-jwt:9.29")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    //contracts
    implementation("com.github.zveronHSe.contract:profile:1.9.7")
    implementation("com.github.zveronHSe.contract:auth:1.9.7")

    //testing
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
    testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock:3.1.5")
}
