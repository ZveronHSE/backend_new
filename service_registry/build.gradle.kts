dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-server:3.1.4")
}

configurations {
    implementation.get().exclude("io.kotest")
    implementation.get().exclude("com.ninja-squad")
    implementation.get().exclude("org.testcontainers")
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-test:2.7.4")
    implementation.get().exclude("org.hibernate")
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-data-jpa:2.7.4")
    implementation.get().exclude("org.postgresql")
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter:2.6.13")
    implementation.get().exclude("org.liquibase", "liquibase-core:4.18.0")
    implementation.get().exclude("org.springframework", "spring-webflux:5.3.24")
    implementation.get().exclude("net.devh")
    implementation.get().exclude("io.grpc")
    implementation.get().exclude("io.projectreactor.netty")
    implementation.get().exclude("org.springframework.boot", "spring-boot-starter-freemarker:2.6.11")
}
