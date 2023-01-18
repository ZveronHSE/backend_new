dependencies {
    implementation("com.github.zveronHSe:contract:feature~ZV-220-apigateway-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")

    compileOnly("com.google.protobuf:protobuf-java-util:3.21.12")
}
