val protobufVersion: String by rootProject
val kotlinxVersion: String by rootProject
val testcontainersVersion: String by rootProject
val springVersion: String by rootProject

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

    //contract
    implementation("com.github.zveronHSe.contract:order:1.9.9")

    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")

    //ен особо понятно, но если вынести зависимость в рутовый градлг, то падает
    testImplementation("com.ninja-squad:springmockk:4.0.0")
}
