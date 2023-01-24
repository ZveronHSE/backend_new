configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.7.8")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.springframework.boot:spring-boot-starter-jetty:2.7.8")
    implementation("com.github.zveronHSe.contract:auth:ed6bd25fed")
    implementation("com.github.zveronHSe.contract:profile:feature~ZV-185-profile-service-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")

    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.4")

    implementation("com.auth0:java-jwt:4.2.2")

    // https://mvnrepository.com/artifact/org.testcontainers/r2dbc
    testImplementation("org.testcontainers:r2dbc:1.16.3")
    // https://mvnrepository.com/artifact/com.ninja-squad/springmockk
    implementation("com.ninja-squad:springmockk:4.0.0")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-jsonSchema
    implementation("com.fasterxml.jackson.module:jackson-module-jsonSchema:2.14.1")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

// https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.1")
// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-parameter-names
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:2.14.1")


    // https://mvnrepository.com/artifact/org.postgresql/r2dbc-postgresql
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")


}