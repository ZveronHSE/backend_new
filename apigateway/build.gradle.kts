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

    //utils
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("com.google.code.gson:gson:2.10.1")

    //database
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:$springVersion")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    //contracts
    implementation("com.github.zveronHSe.contract:auth:99597be969")
    implementation("com.github.zveronHSe.contract:apigateway:1.8.7")

    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")

    //ен особо понятно, но если вынести зависимость в рутовый градлг, то падает
    testImplementation("com.ninja-squad:springmockk:4.0.0")

    // https://mvnrepository.com/artifact/io.zipkin.brave/brave-instrumentation-grpc
    implementation("io.zipkin.brave:brave-instrumentation-grpc:5.15.0")
    // https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-sleuth
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:3.1.7")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

}
