val jooqVersion: String = "3.16.6"
val springVersion: String by rootProject

dependencies {
    // filtering, pagination
    implementation("org.springframework.boot:spring-boot-starter-jooq:$springVersion")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-meta:$jooqVersion")

    // contracts
    implementation("com.github.zveronHSe.contract:specialist:2.0.0")
}
