configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jetty:2.7.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.5.2")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.1")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.7.8")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.postgresql:r2dbc-postgresql:0.9.3.RELEASE")

    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.4")

    implementation("com.github.zveronHSe.contract:profile:1.7.0")
    implementation("com.github.zveronHSe.contract:auth:a5ff052765")
    implementation("com.nimbusds:nimbus-jose-jwt:9.29")

}
