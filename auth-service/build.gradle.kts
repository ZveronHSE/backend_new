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


    //reactive database
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    //utils
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.4")
    implementation("com.nimbusds:nimbus-jose-jwt:9.29")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    //contracts
    implementation("com.github.zveronHSe.contract:profile:1.7.0")
    implementation("com.github.zveronHSe.contract:auth:1.8.2")

    //testing
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")
    testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
}
