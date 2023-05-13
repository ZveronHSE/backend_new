val protobufVersion: String by rootProject
val kotlinxVersion: String by rootProject
val testcontainersVersion: String by rootProject
val springVersion: String by rootProject
val contractVersion: String by rootProject
val jooqVersion: String = "3.16.6"


configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-reactor-netty:$springVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springVersion")

    //kotlinx
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

    //utils
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    //database
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    // jooq
    implementation("org.springframework.boot:spring-boot-starter-jooq:$springVersion")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-meta:$jooqVersion")
    implementation("org.jooq:jooq-meta-extensions:$jooqVersion")
    implementation("org.jooq:jooq-kotlin-coroutines:3.18.3")
    implementation("org.jooq:jooq-kotlin:3.18.3")

    //contract
    implementation("com.github.zveronHSe.contract:order:$contractVersion")
    implementation("com.github.zveronHSe.contract:profile:$contractVersion")
    implementation("com.github.zveronHSe.contract:address:$contractVersion")

    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")
}
