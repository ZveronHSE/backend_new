val protobufVersion: String by rootProject
val kotlinxVersion: String by rootProject

dependencies {
    implementation("com.github.zveronHSe.contract:apigateway:1.5.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$kotlinxVersion")
    //seems like it is integrated with current webflux, had to downgrade version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")

    compileOnly("com.google.protobuf:protobuf-java-util:$protobufVersion")
}
