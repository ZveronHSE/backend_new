val jooqVersion: String = "3.16.6"
val springVersion: String by rootProject
val contractVersion: String = "d393b0cd4f"

dependencies {
    // filtering, pagination
    implementation("org.springframework.boot:spring-boot-starter-jooq:$springVersion")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-meta:$jooqVersion")

    // contracts
    implementation("com.github.zveronHSe.contract:specialist:$contractVersion")
    implementation("com.github.zveronHSe.contract:order:$contractVersion")
}
