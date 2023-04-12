val contractVersion: String = "1.9.7"
val kotlinxVersion: String by rootProject

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra:3.0.4")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")

    implementation("com.github.zveronHSe.contract:chat:$contractVersion")
    implementation("com.github.zveronHSe.contract:profile:$contractVersion")
    implementation("com.github.zveronHSe.contract:lot:$contractVersion")
    implementation("com.github.zveronHSe.contract:blacklist:$contractVersion")
}