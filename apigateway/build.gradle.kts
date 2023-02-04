val protobufVersion: String by rootProject
val kotlinxVersion: String by rootProject
val testcontainersVersion: String by rootProject

configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jetty:2.7.8")


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")
    //seems like it is integrated with current webflux, had to downgrade version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.7.8")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    implementation("com.github.zveronHSe.contract:auth:6129953b3f")
    implementation("com.github.zveronHSe.contract:apigateway:1.5.0")

    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")

    testImplementation("com.ninja-squad:springmockk:4.0.0")
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-slf4j
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    // https://mvnrepository.com/artifact/net.logstash.logback/logstash-logback-encoder
    implementation("net.logstash.logback:logstash-logback-encoder:7.2")

// https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

}
