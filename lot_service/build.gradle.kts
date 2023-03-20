val jooqVersion: String = "3.16.6"
val springVersion: String by rootProject
val protobufVersion: String by rootProject
val contractVersion: String = "1.9.0"

dependencies {
    implementation("com.vladmihalcea:hibernate-types-55:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")

    // filtering, pagination
    implementation("org.springframework.boot:spring-boot-starter-jooq:$springVersion")
    implementation("org.jooq:jooq:$jooqVersion")
    implementation("org.jooq:jooq-meta:$jooqVersion")

    // protobuf
    compileOnly("com.google.protobuf:protobuf-java-util:$protobufVersion")

    // contracts
    implementation("com.github.zveronHse.contract:parameter:$contractVersion")
    implementation("com.github.zveronHSe.contract:favorites:$contractVersion")
    implementation("com.github.zveronHSe.contract:address:$contractVersion")
    implementation("com.github.zveronHSe.contract:profile:$contractVersion")
    implementation("com.github.zveronHSe.contract:lot:461b49455a")
    implementation("com.github.zveronHSe.contract:core:$contractVersion")
}
