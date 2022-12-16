plugins {
}

val springVersion: String by rootProject

dependencies {
    implementation("com.github.zveronHSe.contract:address:1.0.0")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
