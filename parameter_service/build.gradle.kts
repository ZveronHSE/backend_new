val springVersion: String by rootProject
val contractVersion: String by rootProject

dependencies {
    implementation("com.vladmihalcea:hibernate-types-55:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")

    // contract
    implementation("com.github.zveronHse.contract:parameter:$contractVersion")
    implementation("org.springframework.boot:spring-boot-starter-cache:$springVersion")
}
