val contractVersion: String by rootProject
val kotlinxVersion: String by rootProject

dependencies {
    // Data
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")

    // Kotlin reactive
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")

    // Contracts
    implementation("com.github.zveronHSe.contract:chat:$contractVersion")
    implementation("com.github.zveronHSe.contract:profile:$contractVersion")
    implementation("com.github.zveronHSe.contract:lot:$contractVersion")
    implementation("com.github.zveronHSe.contract:blacklist:$contractVersion")
    implementation("com.github.zveronHSe.contract:core:$contractVersion")

    // Logging
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    // Testing
    testImplementation("org.apache.commons:commons-lang3:3.8.1")
    testImplementation("app.cash.turbine:turbine:0.12.3")
}