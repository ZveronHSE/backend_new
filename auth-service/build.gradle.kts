configurations {
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-tomcat")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc:2.7.8")

    implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")

    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")

    implementation("org.springframework.boot:spring-boot-starter-jetty:2.7.8")
    implementation("com.github.zveronHSe.contract:auth:ed6bd25fed")
    implementation("com.github.zveronHSe.contract:profile:feature~ZV-185-profile-service-SNAPSHOT")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    //feature~ZV-185-profile-service-SNAPSHOT
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")

    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.4")

    implementation("com.auth0:java-jwt:4.2.2")
}