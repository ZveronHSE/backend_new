val contractVersion: String = "1.9.5"
val kotlinxVersion: String by rootProject

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-cassandra-reactive")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinxVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")

    implementation("com.github.zveronHSe.contract:chat:93d16e6191")
    implementation("com.github.zveronHSe.contract:profile:$contractVersion")
    implementation("com.github.zveronHSe.contract:lot:$contractVersion")
    implementation("com.github.zveronHSe.contract:blacklist:66ac0dadd1")
    implementation("com.github.zveronHSe.contract:core:$contractVersion")

    testImplementation("org.apache.commons:commons-lang3:3.8.1")
    testImplementation("app.cash.turbine:turbine:0.12.3")
}